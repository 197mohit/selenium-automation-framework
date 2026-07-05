package scripts;

import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.SendOTP;
import com.paytm.api.nativeAPI.ValidateOTP;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.AuthUtil;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;


@Owner("Gagandeep")
@Epic("Sprint-30.2")
@Feature("PGP-19470")
public class RedirectUserIfBalanceInsufficient extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();


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

    //NATIVE - calling fetchPaymentOptions

    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user is redirected to AddandPay cashier page")
    @Parameters({"theme"})
    public void validateWhenWalletAmountLessThanTxnLandsCashierPage(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        WalletHelpers.validateBalance(user, 0.0);
    }


    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-22197")
    @Owner("Tarun")
    @Parameters({"theme"})
    @Test(description = "Verify a successful wallet transaction for ADDandPAY merchant when wallet balance < transaction balance >(deferred flow)")
    public void verifyWalletTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(String.valueOf(txnAmount))
                .build();

       FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setOrderId(orderId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        WalletHelpers.validateBalance(user, 0.0);
    }





    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user able to retry pay through CC")
    @Parameters({"theme"})
    public void validateWhenWalletAmountLessThanTxnPayCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }


    @Epic(Constants.Sprint.SPRINT32_3)
    @Feature("PGP-22197")
    @Owner("Tarun")
    @Parameters({"theme"})
    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user able to retry pay through CC for deferred flow")
    public void validateWhenWalletAmountLessThanTxnPayCCDeferredFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);


        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(String.valueOf(txnAmount))
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setOrderId(orderId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }



    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user able to retry pay through DC")
    @Parameters({"theme"})
    public void validateWhenWalletAmountLessThanTxnPayDC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }
    @Issue("PGP-20772")
    @Parameters({"theme"})
    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user able to retry pay through PPBL",groups = Group.Status.BUG)
    public void validateWhenWalletAmountLessThanTxnPayPPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.PPBL);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user able to retry pay through NB")
    @Parameters({"theme"})
    public void validateWhenWalletAmountLessThanTxnPayNB(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }



    @Test(description = "Verify for AddandPay merchant when wallet amount is less than transaction amount than user able to retry pay through Saved Card")
    @Parameters({"theme"})
    public void validateWhenWalletAmountLessThanTxnPaySavedCard(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        PaymentDTO CorrectSavedCard = new PaymentDTO();
        SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
        CorrectSavedCard.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);;
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }

    @Test(description = "Verify for AddandPay merchant when wallet amount is equal to transaction amount than user is not redirected to cashier page")
    @Parameters({"theme"})
    public void validateWhenWalletAmountEqualToTxnCashierPageIsNotDisplayed(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();





    }


    @Test(description = "Verify for Hybrid merchant when wallet amount is less than transaction amount than user is not redirected to cashier page")
    public void validateWhenWalletAmountLessThanTxnForHybridItshouldNotLandOnCashierPage() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                    .assertAll();
    }


    //Native OTP Validation

    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is less than transaction amount,user would be redirected to cashier page")
    @Parameters({"theme"})
    public void verifyNativeRequestwithOTPValidationRedirectCashierpage(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        WalletHelpers.validateBalance(user, 0.0);
    }


    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is less than transaction amount than user retry payment through CC")
    @Parameters({"theme"})
    public void verifyNativeRequestwithOTPValidationRedirectCashierpagePaywithCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is less than transaction amount than user retry payment through DC")
    @Parameters({"theme"})
    public void verifyNativeRequestwithOTPValidationRedirectCashierpagePaywithDC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is less than transaction amount than user retry payment through SavedCard")
    @Parameters({"theme"})
    public void verifyNativeRequestwithOTPValidationRedirectCashierpagePaywithSaveCard(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        PaymentDTO CorrectSavedCard = new PaymentDTO();
        SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        CorrectSavedCard.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }

    @Issue("PGP-20772")
    @Parameters({"theme"})
    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is less than transaction amount than user retry payment through PPBL",groups = Group.Status.BUG)
    public void verifyNativeRequestwithOTPValidationRedirectCashierpagePaywithPPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.PPBL,Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        WalletHelpers.validateBalance(user, 0.0);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is less than transaction amount user retry payment through NB")
    @Parameters({"theme"})
    public void verifyNativeRequestwithOTPValidationRedirectCashierpagePaywithNB(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Test(description = "Verify for AddandPay merchant With Validate OTP request when wallet amount is equal to transaction amount than user is not redirected to cashier page")
    public void verifyNativeRequestwithOTPValidationNotRedirectCashierpageAmtSame() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user,txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
    }


    @Test(description = "Verify for Hybrid merchant With Validate OTP request when wallet amount is less than transaction amount than user is not redirected to cashier page")
    public void verifyNativeRequestwithOTPValidationNotRedirectCashierpageHybrid() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.00;
        WalletHelpers.modifyBalance(user,txnAmount-1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                    .assertAll();

    }


    //NATIVE_SUBSCRIPTION

    @Test(description = "Verify that subscription when wallet amount is less than txn amount it land back to cashier page incase of Native subscription")
    @Parameters({"theme"})
    public void validateAfterRetrythroughPPIlessBalanceItLandsCashierPage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 5.0d;
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");

    }


    @Test(description = "Verify that subscription when wallet amount is less than txn amount it land back to cashier page in subscription and pay through CC")
    @Parameters({"theme"})
    public void validatePayByCCAfterRetrythroughPPIlessBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 5.0d;
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }


    @Test(description = "Verify that subscription when wallet amount is less than txn amount it land back to cashier page in subscription and pay through DC")
    @Parameters({"theme"})
    public void validatePayByDCAfterRetrythroughPPIlessBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 5.0d;
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }

    @Issue("PGP-20772")
    @Parameters({"theme"})
    @Test(description = "Verify that subscription when wallet amount is less than txn amount it land back to cashier page in subscription and pay through PPBL",groups = Group.Status.BUG)
    public void validatePayByPPBLForSubsAfterRetrythroughPPIlessBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 5.0d;

        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }

    @Test(description = "Verify that subscription when wallet amount is less than txn amount it land back to cashier page in subscription and pay through SavedCard")
    @Parameters({"theme"})
    public void validatePayBySavedCardForSubsAfterRetrythroughPPIlessBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 5.0d;

        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.PPBL);
        PaymentDTO CorrectSavedCard = new PaymentDTO();
        SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());

        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        CorrectSavedCard.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD, CorrectSavedCard);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }

    @Test(description = "Verify that subscription when wallet amount is less than txn amount it land back to cashier page in subscription and pay through NB")
    @Parameters({"theme"})
    public void validatePayByNBForSubsAfterRetrythroughPPIlessBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 5.0d;

        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }



    @Test(description = "Verify that subscription when wallet amount is equal to txn amount it doesn't land back to cashier page")
    public void validateUserNotRedirectWhenBalanceIsEqualToTxnAmountForSubs() throws Exception {
        double txnAmount = 5.0d;

        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        User user = userManager.getForWrite(Label.BASIC);

        WalletHelpers.modifyBalance(user, txnAmount);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);


        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }


    @Test(description = "Verify that subscription when wallet amount is less than txn amount it doesn't land back to cashier page for Hybrid Merchant")
    public void validateUserNotRedirectWhenBalanceIsLessThanEquaTxnAmountForSubsForHybrid() throws Exception {
        double txnAmount = 5.0d;

        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);

        WalletHelpers.modifyBalance(user, txnAmount-1);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                    .assertAll();
    }


    //NATIVE_SUBSCRIPTION (SubsonWallet) OTP flow


    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is less than transaction amount than user is redirected to AddandPay cashier page")
    @Parameters({"theme"})
    public void validateAfterRetrythroughPPIlessBalanceItLandsCashierPageforSubsonSubsWithAutologin(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is less than transaction amount than user retry pay though CC")
    @Parameters({"theme"})
    public void validateAfterRetrythroughPPIlessBalancePayByCCSubsWithAutologin(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }


    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is less than transaction amount than user retry pay though DC")
    @Parameters({"theme"})
    public void validateAfterRetrythroughPPIlessBalancePayByDCSubsWithAutologin(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");

        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.payBy(Constants.PayMode.DC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Issue("PGP-20772")
    @Parameters({"theme"})
    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is less than transaction amount than user retry pay though PPBL",groups = Group.Status.BUG)
    public void validateAfterRetrythroughPPIlessBalancePayByPPBLSubsWithAutologin(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.PPBL);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.payBy(Constants.PayMode.PPBL);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }



    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is less than transaction amount than user retry pay though NB")
    @Parameters({"theme"})
    public void validateAfterRetrythroughPPIlessBalancePayByNBSubsWithAutologin(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }

    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is less than transaction amount than user retry pay though Saved Card")
    @Parameters({"theme"})
    public void validateAfterRetrythroughPPIlessBalancePayBySavedCardSubsWithAutologin(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        PaymentDTO CorrectSavedCard = new PaymentDTO();
        SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());

        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, false);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.assertContainsTitle("Paytm Secure Online Payment Gateway");
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }



    @Test(description = "Verify for AddandPay merchant With Validate OTP request for subs when wallet amount is equal to txn amount cashier Page should not appear")
    public void validateIfAmountIsSameAsTxnThenItShouldNotRedirectToCashierPageSubsWithAutologin() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.Redirectional_Native;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user,txnAmount);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        JsonPath path = Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        WalletHelpers.validateBalance(user, 0.0);
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .assertAll();

    }



    @Test(description = "Verify for Hybrid merchant With Validate OTP request for subs when wallet amount is less than transaction amount than Won't Redirect to cashier Page")
    public void validateIfAmountIsLessthanTxnAmountForHybridItShouldNotRedirectToCashierPageSubsWithAutologin() throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user,txnAmount-1);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();



        InitTxnResponseDTO initTxnResponse = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        SendOTP sendotp = new SendOTP(txnToken, user.mobNo(), initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        sendotp.execute();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //String otp = AuthUtil.getOtp(user.mobNo());
        String otp = "123456";
        ValidateOTP val = new ValidateOTP(txnToken, otp, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        val.execute();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE, subsId)
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

    }


}