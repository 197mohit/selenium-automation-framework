package scripts.AOA;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

public class AOAMaxLife extends PGPBaseTest {
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
    @Owner("AJEESH")
    @Feature("PGP-37533")
    @Test(description = "Verify that AOA to PG MID Conversion is done in when user hits Create Subscription via AOA.")
    public void PGP_37533_VerifythatmidConversiontakesplaceonCreateSubs() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String midConversionText="Changed AOA mid 216820000008098367958 ,to pg mid eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia.log | " +
                "grep \"NativeSubscriptionTransactionRequestProcessor\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(logs).contains(midConversionText);

    }
    @Owner("AJEESH")
    @Feature("PGP-37533")
    @Test(description = "Verify that AOA to PG MID Conversion is done in when user hits PTC")
    public void PGP_37533_VerifythatmidConversiontakesplaceonPTCcall() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String midConversionText="Changed AOA mid 216820000008098367958 ,to pg mid eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia.log | " +
                "grep \"ProcessTransactionUtil\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(logs).contains(midConversionText);
    }

    @Owner("AJEESH")
    @Feature("PGP-37533")
    @Test(description = "Verify that QUERY_PARAMS have pgMID and orderID for MaxLife UPI SUBS")
    public void PGP_37533_VerifythatTxnforPPBLCisonPgMIDandOrderID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String pgMID = "eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateGatewayName("Paytm")
                .validateTxnAmount("10")
                .validatePaymentMode("UPI")
                .validateStatus("SUCCESS")
                .validateSubsId(subsId);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/instaproxy.log | " +
                "grep \"BankCommunicationLogger\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("URL:https://automation-pg-ext.paytm.in/mockbank/PPBL_UPI/upi/validate-address?orderId="+orderDTO.getORDER_ID()+"&mid="+pgMID);
    }

    @Owner("AJEESH")
    @Feature("PGP-37533")
    @Test(description = "Verify that e2e Txn for MaxLife UPI SUBS")
    public void PGP_37533_Verifye2eMaxLifeUPItxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String midConversionText="Changed AOA mid 216820000008098367958 ,to pg mid eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateGatewayName("Paytm")
                .validateTxnAmount("10")
                .validatePaymentMode("UPI")
                .validateStatus("SUCCESS")
                .validateSubsId(subsId);
    }

    @Owner("AJEESH")
    @Feature("PGP-37533")
    @Test(description = "Verify that AOA MID is returned in response of getTxnStatus API.")
    public void PGP_37533_VerifythatAOAMIDisreturnedinGetTxnStatusReponse() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateGatewayName("Paytm")
                .validateTxnAmount("10")
                .validatePaymentMode("UPI")
                .validateStatus("SUCCESS")
                .validateSubsId(subsId);

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("Paytm")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(subsId)
                .AssertAll();
    }

    @Owner("AJEESH")
    @Feature("PGP-37533")
    @Test(description = "Verify that AOA MID is able to initiate Refund.")
    public void PGP_37533_VerifythatAOAMID_isabletoRefund() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String midConversionText="Changed AOA mid 216820000008098367958 ,to pg mid eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateGatewayName("Paytm")
                .validateTxnAmount("10")
                .validatePaymentMode("UPI")
                .validateStatus("SUCCESS")
                .validateSubsId(subsId);

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("Paytm")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateSubsid(subsId)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(Constants.MerchantType.AOA_SUBS_UPI,initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }

    @Owner("AJEESH")
    @Feature("PGP-38073")
    @Test(description = "Verify that AOA MID and Gateway Name is Paytm sent in Peon for Successful Txn")
    public void PGP_38073_VerifyPeonSentforSuccessMaxLifeUPItxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String midConversionText="Changed AOA mid 216820000008098367958 ,to pg mid eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateGatewayName("Paytm")
                .validateTxnAmount("10")
                .validatePaymentMode("UPI")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(subsId)
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"PeonBodyInfo in JSON format\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("MID");
        Assertions.assertThat(logs).contains(merchant.getId());
        Assertions.assertThat(logs).contains("GATEWAYNAME");
        Assertions.assertThat(logs).contains("Paytm");
    }

    @Owner("AJEESH")
    @Feature("PGP-38073")
    @Test(description = "Verify that AOA MID and Gateway Name is Paytm sent in Peon for Failure Txn")
    public void PGP_38073_VerifyPeonSentforFailureMaxLifeUPItxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String midConversionText="Changed AOA mid 216820000008098367958 ,to pg mid eosweE02496420648016";
        Constants.MerchantType merchant = Constants.MerchantType.AOA_SUBS_UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("17")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("20")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(),
                txnToken, PayMethodType.UPI, subsId)
                .setAUTH_MODE("USRPWD")
                .setPayerAccount(new PaymentDTO().getVpa())
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateGatewayName("Paytm")
                .validateTxnAmount("17")
                .validatePaymentMode("UPI")
                .validateStatus("TXN_FAILURE")
                .validateSubsId(subsId);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"PeonBodyInfo in JSON format\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("MID");
        Assertions.assertThat(logs).contains(merchant.getId());
        Assertions.assertThat(logs).contains("GATEWAYNAME");
        Assertions.assertThat(logs).contains("Paytm");
    }
    @Owner("AJEESH")
    @Feature("PGP-38289")
    @Test(description = "Verify that AOA MID and Gateway Name is Paytm sent in Peon for CC Txn")
    public void PGP_38289_VerifyPeonSentforAOA_CCTxn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_PEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("Paytm")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("Paytm")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"PeonBodyInfo in JSON format\"";
        await().pollInterval(Duration.ONE_MINUTE).atMost(Duration.TWO_MINUTES).untilAsserted(() -> Assertions.assertThat((getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd)).contains("MID")));
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("MID");
        Assertions.assertThat(logs).contains(merchant.getId());
        Assertions.assertThat(logs).contains("GATEWAYNAME");
        Assertions.assertThat(logs).contains("Paytm");
    }
    @Owner("AJEESH")
    @Feature("PGP-38289")
    @Test(description = "Verify that AOA MID and Gateway Name is Paytm sent in Peon for DC Txn")
    public void PGP_38289_VerifyPeonSentforAOA_DCTxn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_PEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("Paytm")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("Paytm")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"PeonBodyInfo in JSON format\"";
        await().pollInterval(Duration.ONE_MINUTE).atMost(Duration.TWO_MINUTES).untilAsserted(() -> Assertions.assertThat((getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd)).contains("MID")));
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("MID");
        Assertions.assertThat(logs).contains(merchant.getId());
        Assertions.assertThat(logs).contains("GATEWAYNAME");
        Assertions.assertThat(logs).contains("Paytm");
    }
    @Owner("AJEESH")
    @Feature("PGP-38289")
    @Test(description = "Verify that AOA MID and Gateway Name is Paytm sent in Peon for NB Txn")
    public void PGP_38289_VerifyPeonSentforAOA_NBTxn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_PEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("Paytm")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("Paytm")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"PeonBodyInfo in JSON format\"";
        await().pollInterval(Duration.ONE_MINUTE).atMost(Duration.TWO_MINUTES).untilAsserted(() -> Assertions.assertThat((getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd)).contains("MID")));
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("MID");
        Assertions.assertThat(logs).contains(merchant.getId());
        Assertions.assertThat(logs).contains("GATEWAYNAME");
        Assertions.assertThat(logs).contains("Paytm");
    }
    @Owner("AJEESH")
    @Feature("PGP-38289")
    @Test(description = "Verify that AOA MID and Gateway Name is Paytm sent in Peon for UPI Txn")
    public void PGP_38289_VerifyPeonSentforAOA_UPITxn() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_PEON;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("Paytm")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("Paytm")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"PeonBodyInfo in JSON format\"";
        await().pollInterval(Duration.ONE_MINUTE).atMost(Duration.TWO_MINUTES).untilAsserted(() -> Assertions.assertThat((getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd)).contains("MID")));
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("MID");
        Assertions.assertThat(logs).contains(merchant.getId());
        Assertions.assertThat(logs).contains("GATEWAYNAME");
        Assertions.assertThat(logs).contains("Paytm");
    }
}
