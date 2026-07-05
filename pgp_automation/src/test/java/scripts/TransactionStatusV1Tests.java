package scripts;

import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TransactionStatusV1API;
import com.paytm.api.coft.PTS.GenerateTokenData;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.PayMethodType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Description;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import java.time.Instant;
import java.util.TreeMap;


public class TransactionStatusV1Tests extends PGPBaseTest {

    private final String API_VERSION_V2 = "v2"; // AS per PGP-28428 Transaction status with Version V2 will work for new Flow for polling again and V1 will work as it is.
    private final String API_VERSION_V1 = "v1";
    public static String tokenIndexNumber;
    public static String tokenIndexNumberFlagOFF;
    public static String CUST_ID,CUST_ID2;
    public static User user;

    static Constants.MerchantType merchant = MerchantType.PGOnly;
    static Constants.MerchantType merchant2 = MerchantType.ADDNPAYPEON;     //Not whitelisted in theia.mapMaskedAssetNoToMaskedCardNo

    @BeforeClass
    public void tokenize() throws Exception {
        CUST_ID = RandomStringUtils.randomAlphabetic(4) + Instant.now().toEpochMilli();
        CUST_ID2 = RandomStringUtils.randomAlphabetic(4) + Instant.now().toEpochMilli();

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCardOnMidCustId(merchant,CUST_ID,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),PaymentDTO.VISA_COFT_CARD_NUMBER);
        tokenIndexNumber=SavedCardHelpers.getTin();
        SavedCardHelpers.addCardOnMidCustId(merchant2,CUST_ID2,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),PaymentDTO.VISA_COFT_CARD_NUMBER);
        tokenIndexNumberFlagOFF=SavedCardHelpers.getTin();
    }

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    public void validateChecksum(Response response, OrderDTO orderDTO, String key){
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", orderDTO.getMID());
        treemap.put("ORDERID", orderDTO.getORDER_ID());
        PGPUtil.isChecksumValid(key,treemap,response.jsonPath().getString("body.txnInfo.CHECKSUMHASH"));
    }


    @Test(description = "Verify Txn status and polling status in UPI Success Transaction.")
    public void verifyTransactionStatusinSuccessUPITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddMoney;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        validateChecksum(response, orderDTO, merchantType.getKey());
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateChecksum(response, orderDTO, merchantType.getKey());
    }

    @Test(description = "Verify Txn status and polling status required in UPI Pending Transaction.")
    public void verifyTransactionStatusinPendingUPITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.HDFC_UPI_COLLECT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.46")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT("99.46")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(true).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        validateChecksum(response, orderDTO, merchantType.getKey());
    }

    @Test(description = "Verify Txn status and polling status required in UPI Failure Transaction.")
    public void verifyTransactionStatusinFailureUPITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.HDFC_UPI_COLLECT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.31")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT("99.31")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Failed to retrieve Bank Form");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        validateChecksum(response, orderDTO, merchantType.getKey());
    }



    @Test(description = "Verify Txn status and polling status required in Offline UPI Success Transaction.")
    public void verifyTransactionStatusinUPIOfflineSuccessUPITxn() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        String txnAmount = "2.00";

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchant, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("UPI")
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .setPayerAccount("9999661503@paytm")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant.getId()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("SSO").setToken(user.ssoToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", merchant.getId());
        treemap.put("ORDERID", orderDTO.getORDER_ID());
        PGPUtil.isChecksumValid(merchant.getKey(),treemap,response.jsonPath().getString("body.txnInfo.CHECKSUMHASH"));
    }

    @Test(description = "Verify Txn status and polling status required in Deferred Native UPI Success Transaction.")
    public void verifyTransactionStatusinUPISuccessUPITxnDeferredNative() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        String orderID = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(merchant.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),
                orderID, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setOrderId(orderID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(initTxnDTO.getBody().getMid(),txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();


        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(initTxnDTO.getBody().getMid()).setOrderId(initTxnDTO.orderFromBody()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", merchant.getId());
        treemap.put("ORDERID", orderID);
        PGPUtil.isChecksumValid(merchant.getKey(),treemap,response.jsonPath().getString("body.txnInfo.CHECKSUMHASH"));
    }

    @Test(description = "Verify Txn status and polling status required in UPI Hybrid Failure Transaction.")
    public void verifyTransactionStatusinHybridFailureUPITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PRIORITY_HYBRID_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.31")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT("99.31")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("HYBRID");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Failed to retrieve Bank Form");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        //validateChecksum(response, orderDTO, merchantType.getKey());
    }

    @Test(description = "Verify The lowest timeout value should be picked.")
    public void verifLowestUPIStatusTimeoutValuePicked() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.bankForm.displayField.statusTimeout")).isEqualTo("480000");
    }

    @Test(description = "Verify Txn status and polling status required in Subscription UPI Success Transaction.")
    public void verifyTransactionStatusinNativeSubscriptionUPITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_NATIVE;
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
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

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
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateChecksum(response, orderDTO, merchant.getKey());
    }

    @Test(description = "Verify Txn status and polling status required in NATIVE_MF_SIP Subscription UPI Success Transaction.")
    public void verifyTransactionStatusinNATIVE_MF_SIPSubscriptionUPITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_COLLECT_NATIVEMFSIP;
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
                .setRequestType("NATIVE_MF_SIP")
                .build();


        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "UPI", false);
        Assertions.assertThat(paymodeStatus).as("UPI paymode status mismatch").isTrue();

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
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(true);
        response = transactionStatusV1API.executeUntilPollingRequiredIsFalse();
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateChecksum(response, orderDTO, merchant.getKey());
    }

    @Test(description = "Verify Txn status and polling status won't come in UPI Success Transaction when API has version v1")
    public void verifyTransactionStatusinSuccessUPITxnWithAPIV1Version() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddMoney;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("UPI")
                .setPayerAccount("9999661503@paytm")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V1).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        response = transactionStatusV1API.executeUntilNotPending();
        validateChecksum(response, orderDTO, merchantType.getKey());
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateChecksum(response, orderDTO, merchantType.getKey());
    }



    @Test(description = "Verify Txn status and polling status in CC txn polling Required should be false")
    public void verifyTransactionStatusinSuccessCCITxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setChannelCode("HDFC")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        validateChecksum(response, orderDTO, merchantType.getKey());
        Assertions.assertThat(response.jsonPath().getBoolean("body.isPollingRequired")).isEqualTo(false);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Success Txn via FreshCard")
    public void ValidateLASTFOURDIGITSSuccessTxn1() throws Exception {
        //"FF4J Feature Flag:- theia.mapMaskedAssetNoToMaskedCardNo (mid-based strategy)

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPayerAccount("test@paytm")
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        String CardNumber = PaymentDTO.DEBIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(CardNumber)
                .build();
        ProcessTxnV1Response response1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(CardNumber.substring(CardNumber.length()-4));
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Failure Txn | ONUS Merchant")
    public void ValidateLASTFOURDIGITSFailureTxn2() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        String CardNumber = PaymentDTO.DEBIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(CardNumber)
                .build();
        ProcessTxnV1Response response1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(CardNumber.substring(CardNumber.length() - 4));
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Pending Txn")
    public void ValidateLASTFOURDIGITSPendingTxn3() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String CardNumber = PaymentDTO.DEBIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(CardNumber)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(CardNumber.substring(CardNumber.length() - 4));
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for Success Txn")
    public void ValidateLASTFOURDIGITSNotPresentSuccessTxn4() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADDNPAYPEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPayerAccount("test@paytm")
                .build();

        String CardNumber = PaymentDTO.DEBIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(CardNumber)
                .build();
        ProcessTxnV1Response response1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for Failure Txn")
    public void ValidateLASTFOURDIGITSNotPresentFailureTxn5() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.ADDNPAYPEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String CardNumber = PaymentDTO.DEBIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(CardNumber)
                .build();
        ProcessTxnV1Response response1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        validateChecksum(response, orderDTO, merchantType.getKey());

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for Pending Txn")
    public void ValidateLASTFOURDIGITSNotPresentPendingTxn6() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADDNPAYPEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String CardNumber = PaymentDTO.DEBIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(orderDTO.getMID(),txnToken,orderDTO.getORDER_ID())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(CardNumber)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        validateChecksum(response, orderDTO, merchantType.getKey());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING"); //Pending jst for checking
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Success Txn via TIN")
    public void ValidateLASTFOURDIGITSPresentSuccessTINTxn7() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("10").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        //TO grep LastFourDigit from TokenData
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        String cardLastFourDigit = generteTokenDataResponse.getString("body.tokenInfo.cardSuffix");

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setCustId(CUST_ID)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(tokenIndexNumber+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();               //setMid(orderDTO.getMID()
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(cardLastFourDigit);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Failure Txn via TIN")
    public void ValidateLASTFOURDIGITSPresentFailureTINTxn8() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        String cardLastFourDigit = generteTokenDataResponse.getString("body.tokenInfo.cardSuffix");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("99.99").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(tokenIndexNumber+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(cardLastFourDigit);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Pending Txn via TIN")
    public void ValidateLASTFOURDIGITSPresentPendingTINTxn9() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        String cardLastFourDigit = generteTokenDataResponse.getString("body.tokenInfo.cardSuffix");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("99.84").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(tokenIndexNumber+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(cardLastFourDigit);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for SUCCESS Txn via TIN")
    public void ValidateLASTFOURDIGITSNotPresentSuccessTINTxn10() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant2).setTxnValue("10").setCustId(CUST_ID2).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant2.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(tokenIndexNumberFlagOFF+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant2.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for Failure Txn via TIN")
    public void ValidateLASTFOURDIGITSNotPresentFailureTINTxn11() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant2).setTxnValue("99.99").setCustId(CUST_ID2).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant2.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(tokenIndexNumberFlagOFF+"||123|")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant2.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for PENDING Txn via TIN")
    public void ValidateLASTFOURDIGITSNotPresentPendingTINTxn12() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant2).setTxnValue("99.84").setCustId(CUST_ID2).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant2.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(tokenIndexNumberFlagOFF+"||123|")
                .setAuthMode("otp")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant2.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));

        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Success Txn via TOKEN/TAVV")
    public void ValidateLASTFOURDIGITSPresentSUCCESSTOKENTxn13() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM", tokenIndexNumber, CUST_ID, "").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("10").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                .setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(cardTokenInfo.cardSuffix);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Pending Txn via TOKEN/TAVV")
    public void ValidateLASTFOURDIGITSPresentPendingTOKENTxn14() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("99.84").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(cardTokenInfo.cardSuffix);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for Failure Txn via TOKEN/TAVV")
    public void ValidateLASTFOURDIGITSPresentFailureTOKENTxn15() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant.getId()).buildRequest("ECOM",tokenIndexNumber,CUST_ID,"").generateChecksum(merchant.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("99.99").setCustId(CUST_ID).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(cardTokenInfo.cardSuffix);
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for SUCCESS Txn via TOKEN/TAVV")
    public void ValidateLASTFOURDIGITSNotPresentSuccessTOKENTxn16() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant2.getId()).buildRequest("ECOM",tokenIndexNumberFlagOFF,CUST_ID2,"").generateChecksum(merchant2.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant2).setTxnValue("10").setCustId(CUST_ID2).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant2.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchant2.getId()).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is not present in Response of v1/transactionStatus when FF4J Flag is Toggled OFF for Failure Txn via TOKEN/TAVV")
    public void ValidateLASTFOURDIGITSNotPresentFailureTOKENTxn17() throws Exception {
        GenerateTokenData generateTokenData = new GenerateTokenData(merchant2.getId()).buildRequest("ECOM",tokenIndexNumberFlagOFF,CUST_ID2,"").generateChecksum(merchant2.getKey());
        JsonPath generteTokenDataResponse = generateTokenData.execute().jsonPath();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant2).setTxnValue("99.99").setCustId(CUST_ID2).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant2, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(generteTokenDataResponse.getString("body.cardToken"));
        cardTokenInfo.setTokenExpiry(generteTokenDataResponse.getString("body.tokenInfo.tokenExpiry"));
        cardTokenInfo.setTavv(generteTokenDataResponse.getString("body.tavv"));
        cardTokenInfo.setCardSuffix(generteTokenDataResponse.getString("body.tokenInfo.cardSuffix"));
        cardTokenInfo.setPanUniqueReference(generteTokenDataResponse.getString("body.tokenInfo.panUniqueReference"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant2.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setCardInfo("||123|")
                .setcardTokenInfo(cardTokenInfo)
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo")).doesNotContain("LASTFOURDIGITS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for EMI SUCCESS Txn via Freshcards")
    public void ValidateLASTFOURDIGITSPresentEMITxn18() throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_ON_TOKEN;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        String EMICardNumber = PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI")
                .setCardNum(EMICardNumber)
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchantType.getId()).setOrderId(orderId));      //setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
        transactionStatusV1DTO.setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response1 = transactionStatusV1API.execute();

        Assertions.assertThat(response1.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(EMICardNumber.substring(EMICardNumber.length()-4));
        Assertions.assertThat(response1.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for EMI PENDING Txn via Freshcards")
    public void ValidateLASTFOURDIGITSPresentEMITxn19() throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_ON_TOKEN;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("99.84").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        String EMICardNumber = PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("EMI")
                .setCardNum(EMICardNumber)
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchantType.getId()).setOrderId(orderId));      //setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
        transactionStatusV1DTO.setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response1 = transactionStatusV1API.execute();

        Assertions.assertThat(response1.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(EMICardNumber.substring(EMICardNumber.length()-4));
        Assertions.assertThat(response1.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("PENDING");
    }

    @Feature("PGP-46373")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA: PGP-47750")
    @Test(description = "Validate LASTFOURDIGITS Param is present in Response of v1/transactionStatus when FF4J Flag is Toggled ON for EMI Failure")
    public void ValidateLASTFOURDIGITSPresentEMITxn20() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_ON_TOKEN;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("99.99").build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        String EMICardNumber = PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), initTxnResponseDTO.getBody().getTxnToken(), orderId)
                .setPaymentMode("EMI")
                .setCardNum(EMICardNumber)
                .setAuthMode("otp")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(merchantType.getId()).setOrderId(orderId));      //setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()))
        transactionStatusV1DTO.setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(initTxnResponseDTO.getBody().getTxnToken()));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response1 = transactionStatusV1API.execute();

        Assertions.assertThat(response1.jsonPath().getString("body.txnInfo.LASTFOURDIGITS")).isEqualTo(EMICardNumber.substring(EMICardNumber.length() - 4));
        Assertions.assertThat(response1.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_FAILURE");
    }
}