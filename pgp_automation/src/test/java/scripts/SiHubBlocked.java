package scripts;


import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.AKSHAT;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner(AKSHAT)
public class SiHubBlocked extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

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

    @Parameters({"theme"})
    @Test(description = "To verify that siHubMode is not passed in cashierPay when payMode = UPI")
    public void TC_001_siHubMode_notPassed_forUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
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
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\"siHubMode\"");

    }

    @Parameters({"theme"})
    @Test(description = "To verify that siHubMode is not passed in cashierPay when payMode = Wallet")
    public void TC_002_siHubMode_notPassed_forWalletonly(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchant, theme).build();
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
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
                .validateCheckSum(merchant.getKey())
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\"siHubMode\"");

    }


    @Parameters({"theme"})
    @Test(description = "To verify that siHubMode is not passed in cashierPay when payMode = PPBL")
    public void TC_003_siHubMode_notPassed_forPPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.SUBS_BANK_MANDATE_MID;
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchant, theme)
                .setSUBS_PAYMENT_MODE("PPBL")
                .build();
        User user = userManager.getForWrite(Label.BASIC,Label.LOGIN);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\"siHubMode\"");

    }


    @Test(description = "To verify that siHubMode is not passed in native cashierPay when payMode = PPBL")
    public void TC_004_siHubMode_notPassed_forPPBL() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_BANK_MANDATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
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
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("NB")
                .validateCurrency("INR")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName(Constants.Gateway.PPBL.toString())//"PPBL"
                .validateBankName(Constants.Bank.PPBL.toString())//"PPBL"
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\"siHubMode\"");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "To verify that siHubMode is not passed in native cashierPay when payMode = UPI" )
    public void TC_005_siHubMode_notPassed_forUPI(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
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
                .validatePaymentMode(Constants.PayMode.UPI.toString())//"UPI"
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())//"PPBLC"
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\"siHubMode\"");

    }

    @Test(description = "To verify that siHubMode is not passed in native cashierPay when payMode = PPI")
    public void TC_006_siHubMode_notPassed_forPPI() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Double walletAmount = 5.0;
        WalletHelpers.modifyBalance(user, walletAmount);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
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
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setPaymentFlow("NONE")
                .setAUTH_MODE("USRPWD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
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

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).doesNotContain("\"siHubMode\"");

    }

}