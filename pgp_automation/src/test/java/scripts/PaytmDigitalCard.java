package scripts;

import com.paytm.api.Postpaid;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.POSTPAIDANDUPI;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.appconstants.Constants.Owner.ROHIT;
import static com.paytm.base.test.Group.Status.TO_BE_FIXED;

@Owner("Deepak")
public class PaytmDigitalCard extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String PAYTM_POSTPAID = "PAYTM_DIGITAL_CREDIT";
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();


    @Parameters({"theme"})
    @Test(description = "Paytm Postpaid not visible before login")
    public void PGP_466_PostPaidNotVisibleBeforeLogin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .setSSO_TOKEN("")
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.radioButtonPaytmPostpaid().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Paytm Postpaid visible after login")
    public void PGP_467_PostPaidVisibleAfterLogin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.radioButtonPaytmPostpaid().getCssValue("display").equals("block"));
    }

    @Parameters({"theme"})
    @Test(description = "Paytm Postpaid option inactive when txn amt > postpaid balance")
    public void PGP_468_PostPaidBalanceLessThanTxn(@Optional("merchant4") String theme) throws Exception {
        Postpaid pp = new Postpaid("2");
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.AddnPay, theme, user)
                .setTXN_AMOUNT("1")
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        cashierPage.radioButtonPaytmPostpaidClickable().assertDisabled();
    }

    @Parameters({"theme"})
    @Test(description = "Paytm Postpaid and wallet are inactive when txn amt > postpaid and wallet balance is zero")
    public void PGP_517_PostPaidAndWalletInactive(@Optional("merchant4") String theme) throws Exception {
        Postpaid pp = new Postpaid("2");
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setTXN_AMOUNT("3")
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 0.0);
        checkoutPage.createOrder(orderDTO);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaidClickable().assertDisabled();
        cashierPage.radioButtonWallet().assertDeSelected();
    }

    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm post paid using PGOnlymerchant when user enters correct passcode")
    public void PGP_469successfulPostPaidTxn_fromPGOnly(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PGOnly, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(PAYTM_POSTPAID)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Success Txn from Paytm post paid when user enters correct passcode")
    public void PGP_469_successfulPostPaidTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(POSTPAIDANDUPI, theme, user)
                .build();
        PostpaidHelpers.updateBalance("2.00");
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(PAYTM_POSTPAID)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateBankName(Constants.Gateway.PAYTMCC.toString())
                .validateCheckSum(Constants.MerchantType.POSTPAIDANDUPI.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(PAYTM_POSTPAID)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //Disabling this test case and passcode not enabled on automation env.
//    @Parameters({"theme"})
//    @Test(description = "Verify message when invalid pass code entered",enabled = false)
    public void PGP_470_InvalidPassCode(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 2.0);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setPasscode("9090");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        cashierPage.assertContainsText(Constants.MessageAssert.POSTPAID_INCORRECT_PASSCODE.toString());
    }
    

    //Disabling this test case and passcode not enabled on automation env.
//    @Parameters({"theme"})
//    @Test(description = "Verify message when invalid pass code entered for PGOnly merchant", enabled = false)
    public void PGP_470_InvalidPassCode_PgOnly(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PGOnly, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 2.0);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setPasscode("3456");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        cashierPage.assertContainsText(Constants.MessageAssert.POSTPAID_INCORRECT_PASSCODE.toString());
    }

    //Disabling this test case and passcode not enabled on automation env.
//    @Parameters({"theme"})
//    @Test(description = "Verify message when blank pass code entered", enabled = false)
    public void PGP_516_BlankPasscode(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        PostpaidHelpers.updateBalance("2.00");
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setPasscode("");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        cashierPage.assertContainsText(Constants.MessageAssert.POSTPAID_EMPTY_PASSCODE.toString());
    }

    @Parameters({"theme"})
    @Test(description = "Paytm Postpaid should be Selected on cashier page when wallet balance is zero")
    public void PGP_514_PostPaidSelectWhenWalletBalanceZero(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID);
        WalletHelpers.modifyBalance(user, 0.0);
        SavedCardHelpers.deleteSavedCard(user);
        String txnAmt = "1001";//To allay case where ppbl gets selected instead of postpaid
        PostpaidHelpers.updateBalance("1009");
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT(txnAmt)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        if ("enhancedweb_revamp".equalsIgnoreCase(theme)) {
            cashierPage.radioButtonPostpaid().assertSelected();
            return;
        }
        cashierPage.radioButtonPaytmPostpaidClickable().assertEnabled();
    }

    @Parameters({"theme"})
    @Test(description = "Wallet should be selected when wallet balance is present in user account")
    public void PGP_515_WalletSelectWhenWalletBalanceNotZero(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);

        if ("enhancedweb_revamp".equalsIgnoreCase(theme)) {
            cashierPage.checkBoxPPI().assertChecked();
            return;
        }

        cashierPage.radioButtonWalletChecked().assertEnabled();
    }

 //   @Parameters({"theme"})
 //   @Test(description = "Verify message when invalid passcode entered more than twice.", enabled = false)
    public void PGP_472_WrongPasscodeEnteredTwice(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setPasscode("2321");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        paymentDto.setPasscode("2322");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        paymentDto.setPasscode("2323");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        cashierPage.assertContainsText("To complete the payment enter your Paytm Passcode");
    }

    @Parameters({"theme"})
    @Test(description = "Paytm Postpaid option not visible if PAYTMCC not enabled on merchant")
    public void PGP_473_PaytmCCNotEnabledOnMerchant(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.WalletOnly, theme, user)
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        cashierPage.radioButtonPaytmPostpaid().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "\"Paytm Postpaid\" option not visible when paytm_CC is not enabled for user")
    public void PGP_474_PaytmCCNotEnabledOnUser(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.radioButtonPaytmPostpaid().assertNotVisible();
    }

 //   @Parameters({"theme"})
 //   @Test(description = "Successful Paytm Postpaid Txn with ssoToken", enabled = false)
    public void PGP_477_SuccessfulPostPaidTxnWhenPaytmScopeTokenPassedInCheckoutPage(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setPAYTM_TOKEN(user.paytmToken())
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.0);
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(PAYTM_POSTPAID)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    // Not Automated
 //   @Parameters({"theme"})
 //   @Test(enabled = false, description = "Unsuccessful Postpaid Txn when Wallet scope token is passed in place of Paytm scope token")
    public void PGP_518_UnsuccessfulPostPaidTxnWhenWalletScopeTokenPassedInPlaceOfPaytmScopeInCheckoutPage(@Optional("merchant4") String theme) {
        throw new SkipException("Client need to be updated at Auth End to execute this case");
        /*String mobile = UserManager.getUser();
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme)
                .setTXN_AMOUNT("1")
                .setPAYTM_TOKEN(AuthHelpers.getSSOToken(mobile))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(mobile, 1.0);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        cashierPage.assertContainsText("Invalid credentials");*/
    }

    @Parameters({"theme"})
    @Test(description = "Pending Txn when Paytm Postpaid system is down") // Need to check txn status getting fail
    public void PGP_478_Pending(@Optional("enhancedwap") String theme) throws Exception {
        //Postpaid pp = new Postpaid("100");
        PostpaidHelpers.updateBalance("9");
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setTXN_AMOUNT("9")
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDto = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        new ResponsePage().waitUntilLoads();
        //The txn is going in pending state only, but as the MID is having 0 retries configured so logic is written in theia
        //that if pay mode is postapid and 0 retry is there on MID then internal close order will be called and txn will be
        //moved to failed state.
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateTxnType("SALE")
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Postpaid txn is marked Pending if no response from Postpaid system") // // Need to check txn status getting fail
    public void PGP_482_NoResponseFromPaytmCC(@Optional("enhancedwap") String theme) throws Exception {
        //Postpaid PPObj = new Postpaid("100");
        PostpaidHelpers.updateBalance("100");
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("67")
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDto = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD, paymentDto);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateRespCode("400")
                .validateRespMsg("Transaction status not confirmed yet.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Given user has insufficient postpaid balance When user clicks on postpaid check balance in Native Enhanced WAP theme Then insufficient balance msg is displayed")
    public void TC_insuffBalShownWhenUserClicksCheckBalGivenInsuffPostpaidBal(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        PostpaidHelpers.updateBalance("2.00");
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setTXN_AMOUNT("4")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        /*   https://jira.mypaytm.com/browse/PGP-41941
             Default checked is postpaid and checkbalance autometically called
             cashierPage.btnCheckBalancePostpaid().click();
         */
        cashierPage.lblInsufficientBalancePostpaid().assertVisible();
    }

    @Owner(ROHIT)
    @Feature("PGP-35633")
    @Parameters({"theme"})
    @Test(description = "Signup to Paytm Postpaid banner should be visible, when merchant has postpaid and POSTPAID_ENABLED_ON_MERCHANT perf is Y, and user dont have postpaid ")
    public void postpaidSignupBannerVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.postpaidSignUpStrip().assertVisible();
    }

    @Owner(ROHIT)
    @Feature("PGP-35633")
    @Parameters({"theme"})
    @Test(description = "Signup to Paytm Postpaid banner should not be visible, when merchant has postpaid and POSTPAID_ENABLED_ON_MERCHANT perf is Y, and user have postpaid ")
    public void postpaidSignupBannerNotVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(POSTPAIDANDUPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.postpaidSignUpStrip().assertNotVisible();


    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-39584")
    @Test(description = "Support to show Postpaid balance in the FPO API, merchant has postpaid , and user has postpaid")
    public void showPostpaidbalanceFPOAPI() throws Exception {
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        PostpaidHelpers.updateBalance("2.00");
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getBody().setFetchPaytmInstrumentsBalance("true");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Paytm Postpaid' }[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo("2.00");
    }




    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-39640")
    @Test(description = "Support to show Postpaid balance in the FPO API,Merchant & user have postpaid and fetchPaytminstrumentsBalance is false")
    public void showPostpaidbalanceFPOAPIFetchPaytmInstrumentsBalancefalse() throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getBody().setFetchPaytmInstrumentsBalance("false");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Paytm Postpaid' }[0].payChannelOptions[0].balanceInfo")).isEqualTo(null);
    }



    @Owner(Constants.Owner.VIKASH_VERMA)
    @Feature("PGP-39640")
    @Test(description = "Support to show Postpaid balance in the FPO API,Merchant & user have postpaid and fetchPaytminstrumentsBalance is null")
    public void showPostpaidbalanceFPOAPIFetchPaytmInstrumentsBalanceisnull() throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getBody().setFetchPaytmInstrumentsBalance("");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.displayName == 'Paytm Postpaid' }[0].payChannelOptions[0].balanceInfo")).isEqualTo(null);
    }

    @Parameters({"theme"})
    @Owner(HIMANSHU)
    @Test(description = "Paytm Postpaid not visible before login: checkoutJS")
    public void PostPaidNotVisibleBeforeLoginCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.NATIVE_HYBRID.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().assertNotVisible();
    }
    @Parameters({"theme"})
    @Owner(HIMANSHU)
    @Test(description = "Paytm Postpaid visible before login: checkoutJS")
    public void PostPaidVisibleAfterLoginCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.NATIVE_HYBRID.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.radioButtonPaytmPostpaid().getCssValue("display").equals("block"));
    }

    @Parameters({"theme"})
    @Feature("PGP-47863")
    @Owner(HIMANSHU)
    @Test(description = "Paytm frozen postpaid account error message Checkoutjs flow")
    public void postpaidMessage_frozenAccount(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).setTxnValue("22")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.NATIVE_HYBRID.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().click();
        Assertions.assertThat(cashierPage.PostpaidErrorMessage().getText().equals("Postpaid account is temporarily blocked due to non-payment of bill by due date. Pay your dues to continue using postpaid."));

    }
    @Parameters({"theme"})
    @Feature("PGP-47863")
    @Owner(HIMANSHU)
    @Test(description = "Paytm blocked postpaid account error message Checkoutjs flow")
    public void postpaidMessage_blockedAccount(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).setTxnValue("21")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.NATIVE_HYBRID.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().click();
        Assertions.assertThat(cashierPage.PostpaidErrorMessage().getText().equals("Postpaid account is suspended due to non-payment of bill for more than 30 days. Non payment of dues impacts your CIBIL Score."));
    }

    @Parameters({"theme"})
    @Feature("PGP-47863")
    @Owner(HIMANSHU)
    @Test(description = "Paytm expired postpaid account error message Checkoutjs flow")
    public void postpaidMessage_expiredAccount(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).setTxnValue("23")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);;
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.NATIVE_HYBRID.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().click();
        Assertions.assertThat(cashierPage.PostpaidErrorMessage().getText().equals("Validity of your postpaid account is expired. Pay your outstanding dues and signup again to continue using postpaid"));
    }
}
