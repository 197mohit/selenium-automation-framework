package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.QRHelper;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ankitarora on 15/01/2020
 */

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Ankit Arora")
public class merchantLimitMigration extends PGPBaseTest {

    private static final String JSON_POST_URL = "/checkoutpage/nplus_page.jsp?ttype=hold&jsonresp=";
    private static final String theme = "enhancedweb";
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);

    }

    private OrderDTO validateDynamicQRAndCreateOrder(Constants.MerchantType merchantType, User user) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_CASHIER", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "DYNAMIC_QR_2FA", "N");
        }

        OrderDTO orderDTO = QRHelper.generateDynamicQROrder(merchantType, theme, user);
        return orderDTO;
    }

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, OrderDTO orderDTO, String paymentMode) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = QRHelper.fetchPaymentOptionResponse(user, orderDTO, false);
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }


    /*  Merchant Limit Migration - Test Cases */

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with CC on a merchant with daily txn limit breached - Txn should not be succcessful", groups = "smoke")
    public void PGP_1_dailylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DAILY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.validatedailybreachMsg();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with CC on a merchant with weekly txn limit breached - Txn should not be succcessful", groups = "smoke")
    public void PGP_2_weeklylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.WEEKLY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.validateweeklybreachMsg();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with CC on a merchant with monthly txn limit breached - Txn should not be succcessful", groups = "smoke")
    public void PGP_3_monthlylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MONTHLY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.validatemonthlybreachMsg();

    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with NB on a merchant with daily txn limit breached - Txn should be succcessful", groups = "smoke")
    public void PGP_4_dailylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DAILY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with NB on a merchant with weekly txn limit breached - Txn should be succcessful", groups = "smoke")
    public void PGP_5_weeklylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_DISCOVERY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.pause(2);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                //.validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateBankName(Constants.Bank.ICICINB.toString())
                //.validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with NB on a merchant with monthly txn limit breached - Txn should be succcessful", groups = "smoke")
    public void PGP_6_monthlylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MONTHLY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with UPI on a merchant with daily txn limit breached - Txn should be succcessful", groups = "smoke")
    public void PGP_7_dailylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DAILY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with UPI on a merchant with weekly txn limit breached - Txn should be succcessful", groups = "smoke")
    public void PGP_8_weeklylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.WEEKLY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Perform enhanced-native txn with UPI on a merchant with monthly txn limit breached - Txn should be succcessful", groups = "smoke")
    public void PGP_9_monthlylimitenhanced(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MONTHLY, theme).setTXN_AMOUNT("120").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate add & pay via CC is failed when daily limit is only pending for 10 rs and txn is made for 11 rs.", groups = {"smoke"})
    public void PGP_10_addnpaydaily(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.ADD_DAILY, theme, user).setTXN_AMOUNT("11").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        cashierPage.validatedailybreachMsg();
    }

    @Parameters({"theme"})
    @Test(description = "Validate add & pay via CC is failed when weekly limit is only pending for 10 rs and txn is made for 11 rs.", groups = {"smoke"})
    public void PGP_11_addnpayweekly(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.ADD_WEEKLY, theme, user).setTXN_AMOUNT("11").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        cashierPage.validateweeklybreachMsg();
    }

    @Parameters({"theme"})
    @Test(description = "Validate add & pay via CC is failed when monthly limit is only pending for 10 rs and txn is made for 11 rs.", groups = {"smoke"})
    public void PGP_12_addnpaymonthly(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.ADD_MONTHLY, theme, user).setTXN_AMOUNT("11").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        cashierPage.validatemonthlybreachMsg();
    }


    @Parameters({"theme"})
    @Test(description = "Validate hybrid via CC is failed when daily limit is only pending for 10 rs and txn is made for 20 rs.", groups = {"smoke"})
    public void PGP_13_hybdaily(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.HYB_DAILY, theme, user)
                .setTXN_AMOUNT("20.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        cashierPage.validatedailybreachMsg();
    }

    @Parameters({"theme"})
    @Test(description = "Validate hybrid via CC is failed when weekly limit is only pending for 10 rs and txn is made for 20 rs.", groups = {"smoke"})
    public void PGP_14_hybweekly(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.HYB_WEEKLY, theme, user)
                .setTXN_AMOUNT("20.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        cashierPage.validateweeklybreachMsg();
    }

    @Parameters({"theme"})
    @Test(description = "Validate hybrid via CC is failed when monthly limit is only pending for 10 rs and txn is made for 20 rs.", groups = {"smoke"})
    public void PGP_15_hybmonthly(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.HYB_MONTHLY, theme, user)
                .setTXN_AMOUNT("20.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        cashierPage.validatemonthlybreachMsg();
    }

    /* New Offline */

    Constants.MerchantType dailymerchant = MerchantType.DAILY;

    @Test(description = "Perform an offline txn with DC on a merchant with daily txn limit breached - Txn should not be successful")
    public void PGP_16_offline_dc() throws Exception {
        User user=userManager.getForRead(Label.LIMIT);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(dailymerchant.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(dailymerchant.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(dailymerchant,"SSO",user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("120"))
                .setExtendInfoOrderAlreadyCreated(false)
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210901675242|834|042021")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Payment failed as merchant has crossed his daily acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS"))
                .isEqualTo("TXN_FAILURE");

    }

    @Test(description = "Perform an offline txn with NB on a merchant with daily txn limit breached - Txn should be successful")
    public void PGP_17_offline_nb() throws Exception {
        User user=userManager.getForRead(Label.LIMIT);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(dailymerchant.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(dailymerchant.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(dailymerchant,"SSO",user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("120"))
                .setExtendInfoOrderAlreadyCreated(false)
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(dailymerchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount("120")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .AssertAll();

    }

    Constants.MerchantType weeklymerchant = MerchantType.WEEKLY;

    @Test(description = "Perform an offline txn with DC on a merchant with weekly txn limit breached - Txn should not be successful")
    public void PGP_18_offline_dc() throws Exception {
        User user=userManager.getForRead(Label.LIMIT);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(weeklymerchant.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(weeklymerchant.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(weeklymerchant,"SSO",user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("120"))
                .setExtendInfoOrderAlreadyCreated(false)
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210901675242|834|042021")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Payment failed as merchant has crossed his weekly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS"))
                .isEqualTo("TXN_FAILURE");

    }

    @Test(description = "Perform an offline txn with NB on a merchant with weekly txn limit breached - Txn should be successful")
    public void PGP_19_offline_nb() throws Exception {
        User user=userManager.getForRead(Label.LIMIT);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(weeklymerchant.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(weeklymerchant.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(weeklymerchant,"SSO",user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("120"))
                .setExtendInfoOrderAlreadyCreated(false)
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(weeklymerchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount("120")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .AssertAll();

    }

    Constants.MerchantType monthlymerchant = MerchantType.MONTHLY;

    @Test(description = "Perform an offline txn with DC on a merchant with monthly txn limit breached - Txn should not be successful")
    public void PGP_20_offline_dc() throws Exception {
        User user=userManager.getForRead(Label.LIMIT);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(monthlymerchant.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(monthlymerchant.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(monthlymerchant,"SSO",user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("120"))
                .setExtendInfoOrderAlreadyCreated(false)
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210901675242|834|042021")
                .setAuthMode("otp")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
                .isEqualTo("Payment failed as merchant has crossed his Monthly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS"))
                .isEqualTo("TXN_FAILURE");

    }

    @Test(description = "Perform an offline txn with NB on a merchant with monthly txn limit breached - Txn should be successful")
    public void PGP_21_offline_nb() throws Exception {
        User user=userManager.getForRead(Label.LIMIT);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(monthlymerchant.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(monthlymerchant.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        TxnAmount amount = new TxnAmount();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(monthlymerchant,"SSO",user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amount.setValue("120"))
                .setExtendInfoOrderAlreadyCreated(false)
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(monthlymerchant.getId(),orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount("120")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .AssertAll();

    }

    /* DYNAMIC QR */

    Constants.MerchantType dynamicDaily = MerchantType.DYNAMIC_DAILY;

    @Test(description = "Perform an dynamicqr txn with DC on a merchant with daily txn limit breached - Txn should not be successful")
    public void PGP_22_dynamicqr_dc() throws Exception {
        String paymentMode = "DEBIT_CARD";
        User user = userManager.getForRead(Label.LIMIT);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(dynamicDaily, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        PaymentDTO paymentDTO = new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.asString()).as("PTC response doesn't contain expected message").contains("Payment failed as merchant has crossed his daily acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");

    }

    @Test(description = "Perform an dynamicqr txn with net banking on a merchant with daily txn limit breached - Txn should be successful")
    public void PGP_23_dynamicqr_nb() throws Exception {
        String paymentMode = "NET_BANKING";
        User user = userManager.getForRead(Label.LIMIT);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(dynamicDaily, user);
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
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

    }

    Constants.MerchantType dynamicWeekly = MerchantType.DYNAMIC_WEEKLY;

    @Test(description = "Perform an dynamicqr txn with DC on a merchant with weekly txn limit breached - Txn should not be successful")
    public void PGP_24_dynamicqr_dc() throws Exception {
        String paymentMode = "DEBIT_CARD";
        User user = userManager.getForRead(Label.LIMIT);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(dynamicWeekly, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        PaymentDTO paymentDTO = new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.asString()).as("PTC response doesn't contain expected message").contains("Payment failed as merchant has crossed his weekly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");

    }

    @Test(description = "Perform an dynamicqr txn with net banking on a merchant with weekly txn limit breached - Txn should be successful")
    public void PGP_25_dynamicqr_nb() throws Exception {
        String paymentMode = "NET_BANKING";
        User user = userManager.getForRead(Label.LIMIT);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(dynamicWeekly, user);
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
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

    }

    Constants.MerchantType dynamicMonthly = MerchantType.DYNAMIC_MONTHLY;

    @Test(description = "Perform an dynamicqr txn with DC on a merchant with monthly txn limit breached - Txn should not be successful")
    public void PGP_26_dynamicqr_dc() throws Exception {
        String paymentMode = "DEBIT_CARD";
        User user = userManager.getForRead(Label.LIMIT);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(dynamicMonthly, user);
        fetchPaymentOpt(user, orderDTO, paymentMode);
        PaymentDTO paymentDTO = new PaymentDTO();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setCardNum(paymentDTO.getDebitCardNumber())
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.asString()).as("PTC response doesn't contain expected message").contains("Payment failed as merchant has crossed his Monthly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");

    }

    @Test(description = "Perform an dynamicqr txn with net banking on a merchant with monthly txn limit breached - Txn should not be successful")
    public void PGP_27_dynamicqr_nb() throws Exception {
        String paymentMode = "NET_BANKING";
        User user = userManager.getForRead(Label.LIMIT);
        OrderDTO orderDTO = validateDynamicQRAndCreateOrder(dynamicMonthly, user);
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
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

    }

}
