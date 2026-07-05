package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
import groovy.json.JsonSlurper;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.DEEPAK;
import static com.paytm.appconstants.Constants.Owner.GAURAV;
import static com.paytm.base.test.Group.Status.BUG;

@Owner("Deepak")
public class AddMoneyUber extends PGPBaseTest implements IAddMoney{

    private static final String OOPS_PAGE_MESSAGE = "You are lost in Space.";

    @Parameters({"theme"})
    @Test(description = "Verify successful txn when payment is done via CC", groups = {"smoke"})
    public void PGP_285_successfulAddMoneyCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateTxnDate(new Date())
                .validateCheckSum(MerchantType.AddMoney.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    // Disabling test case as same is invalid
    @Parameters({"theme"})
    @Test(description = "Verify txn when payment is done via CC For 0 Amt")
    public void PGP_285_AddMoneyFor0Amt(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).setTXN_AMOUNT("0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateStatus("TXN_FAILURE").assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful txn when payment is done via DC", groups = {"smoke"})
    public void PGP_285_successfulAddMoneyDC(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.AddMoney.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }



    @Parameters({"theme"})
    @Test(description = "Verify successful Card Saved when payment done Via CC", groups = {"smoke"})
    public void PGP_286_successfulSavedCardAddMoneyCC(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC_WITH_SAVECARD, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.AddMoney.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        OrderDTO orderDTO2 = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        CheckoutPage checkoutPage2 = new CheckoutPage();
        checkoutPage2.createOrder(orderDTO2);
        CashierPage cashierPage2 = CashierPageFactory.getCashierPage(theme);
        cashierPage2.assertSavedCardVisibility();
    }


    @Parameters({"theme"})
    @Test(description = "Verify successful txn when payment is done via NB for greater than 2000 Rs")
    public void PGP_287_AddMoneyTxnUsingNB(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).setTXN_AMOUNT("2000").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.ICICINB.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    // This is controlled by this property -> minimum.amount.for.NB.as.paymode=0, Hence NB should be visible
    @Parameters({"theme"})
    @Test(description = "Verify successful txn when payment is done via NB for less than 2000 Rs")
    public void PGP_287_AddMoneyTxnUsingNBLessThan2000(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).setTXN_AMOUNT("1999").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().assertVisible();
    }


    @Parameters("theme")
    @Test(description = "Verify successful transaction when correct SSOToken is passed.")
    public void PGP_411_validateTxnIsAllowedWhenValidSSOTokenIsPassed(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.PGOnly, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Parameters("theme")
    @Test(description = "Verify failure when bank has insufficient fund.")
    public void PGP_410_VerifyFailureWhenBankHasInsufficientFunds(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT("99.94")//txn amt set to 99.94 to trigger insufficient amt condition in mock
                .build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


//    @Issue("PGP-12341")//Functionality Deprecated
//    @Parameters({"theme"})
//    @Test(description = "Verify that transaction should not be allowed for duplicate order ID.", groups = {"bug"}, enabled = false)
    public void PGP_412_validateTxnNotAllowedWhenDuplicateOrderIdIsPassed(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        new CheckoutPage().createOrder(orderDTO);
        new CheckoutPage().createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("325")
                .validateRespMsg("Duplicate order id.")
                .validateStatus("TXN_FAILURE");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that transaction should not be allowed with expired SSO Token.")
    public void PGP_413_validateTxnNotAllowedWhenExpiredSSoTokenIsPassed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String activeSSOToken = user.ssoToken();
        try {
            AuthHelpers.logout(activeSSOToken);
        }finally {
            user.purge();
        }
        String expiredSSOToken = activeSSOToken;
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setSSO_TOKEN(expiredSSOToken)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        ResponsePage responsePage=new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
    }

    @Issue("PGP-14961")
    @Parameters({"theme"})
    @Test(description = "Verify that transaction should not be allowed for Invalid Channel.", groups = BUG)
    public void PGP_414_validateTxnNotAllowedWhenInvalidChannelIsPassed(@Optional("merchant4") String theme) throws Exception {
        String invalidChannel = "WWW";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setCHANNEL_ID(invalidChannel)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        new LostInSpacePage().imgLostInSpace().assertVisible();
    }

    @Issue("PGP-18102")
    @Parameters({"theme"})
    @Test(description = "Verify that only active acquiring of merchant must display as payment mode at cashier page", groups = {BUG})
    public void PGP_415_1_verifyOnlyActiveAcquiringsAreShownAsPayModes(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT("2000")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.tabCOD().assertNotVisible();
    }


    @Issue("PGP-18102")
    @Parameters({"theme"})
    @Test(description = "Verify that only active acquiring of merchant must display as payment mode at cashier page", groups = {BUG})
    public void PGP_415_verifyOnlyActiveAcquiringsAreShownAsPayModes(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabNetBanking().assertNotVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.tabCOD().assertNotVisible();
    }

    private void validateFailureResponsePage(String theme) {
        ResponsePage responsePage = new ResponsePage();
        if (theme.equalsIgnoreCase("merchant4")) {
            LostInSpacePage lostInSpacePage = new LostInSpacePage();
            lostInSpacePage.imgLostInSpace().assertVisible();
        } else if (theme.equalsIgnoreCase("enhancedweb")) {
            responsePage.validateStatus("TXN_FAILURE");
        } else if (theme.equalsIgnoreCase("enhancedwap")) {
            responsePage.validateStatus("TXN_FAILURE");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify failure when any mandatory param is blank.")
    public void PGP_416_VerifyFailureWhenSSOTokenIsNotPassed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String blankSSOToken = "";
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setSSO_TOKEN(blankSSOToken)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        validateFailureResponsePage(theme);
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful saved card transaction.")
    public void PGP_417_verifyTxnSuccessfullyProcessedWhenPaymentViaSavedCard(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC_ONLY.toString())
                        .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Issue("PGP-13109")
    @Parameters({"theme"})
    @Test(description = "Verify that Txn details are getting reflected correctly in the response page")
    public void PGP_418_verifyTxnDetailsShownInResponsePageAreAsExpected(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();
    }


//    @Parameters("theme")
//    @Test(description = "Verify txn is successful when payment is done via UPI", enabled = false)
    public void PGP_419_verifySuccessfulTxnWhenPaymentViaUPI(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                //.validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(ValidationType.EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Parameters("theme")
    @Test(description = "Verify txn is successful when payment is done via DC")
    public void PGP_420_verifySuccessfulTxnWhenPaymentViaDC(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        WalletHelpers.setZeroBalance(user);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Parameters("theme")
    @Test(description = "Verify transaction fails when Request Type is passed other than ADD_MONEY")
    public void PGP_422_VerifyTxnFailureWhenRequestTypeOtherThanAddMoney(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String nonAddMoneyRequestType = "NO_ADDMONEY";
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.ADD_MONEY_ONLY, theme, user)
                .setREQUEST_TYPE(nonAddMoneyRequestType)
                .build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage("Oops page is not displayed").containsIgnoringCase(OOPS_PAGE_MESSAGE);

    }

    @Parameters({"theme"})
    @Test(description = "Verify oops page should be displayed for Add Money txn when limit is reached before txn", groups = Group.Status.TO_BE_FIXED)
    public void PGP_409_AddMoneyLimitBreachedBeforeTxn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        try {
            OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
            WalletHelpers.breachAddMoneyLimit(user);
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createOrder(orderDTO);
            new OopsPage().waitUntilLoads();
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify add money txn should fail when limit is reached just before pay now button", groups = Group.Status.TO_BE_FIXED)
    public void PGP_423_AddMoneyLimitBreachedBeforePaynow(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        try {
            OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            WalletHelpers.breachAddMoneyLimit(user);
            cashierPage.payBy(PayMode.DC);
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                    .validateBankTxnId(ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_FAILURE")
                    .validateTxnType("ADDMONEY")
                    .validateGatewayName(Gateway.HDFC.toString())
                    .validateRespCode("227")
                    .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                    .validateBankName(Bank.HDFC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("DC")
                    .validateRefundAmnt("0.0")
                    .validateTxnDate(new Date())
                    .AssertAll();
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate failed add money Txn")
    public void addMoneyFail(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT("99.98").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_FAILURE")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful saved card Retry transaction.")
    public void validateAddMoneyUsingRetrySavedCard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.ADD_MONEY_WITH_RETRY, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentDTO incorrectPaymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user,incorrectPaymentDTO.getExpMonth(),incorrectPaymentDTO.getExpYear(),incorrectPaymentDTO.getCreditCardNumber());

        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(PayMode.SAVED_CARD,incorrectPaymentDTO,incorrectPaymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(PayMode.SAVED_CARD,incorrectPaymentDTO,incorrectPaymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }

        cashierPage.payBy(PayMode.SAVED_CARD,incorrectPaymentDTO,paymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFCSC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Parameters({"theme"})
    @Test(description = "test txn < 1 not allowed")
    public void testTxnOfLessThan1NotAllowed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        MerchantType merchant = MerchantType.AddMoney;
        OrderDTO orderDTO = new OrderFactory.AddMoney(merchant, theme, user)
                .setTXN_AMOUNT("0.99")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateCheckSum(merchant.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .AssertAll();
    }

/*    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test(enabled = false) */
    //TODO disabling it because as per Nitin this feature is not supported for AddMoney as of now but may be supported in Phase 2 which is under development
    public void testCardIndexNoIsPresentInOutputWhenCardTokenRequiredIsTrueInOrder(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO order = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setCardTokenRequired(true)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        assertion.apply(responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO));

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NON_EMPTY).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO));
    }

/*    @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test(enabled = false) */
    //TODO disabling it because as per Nitin this feature is not supported for AddMoney as of now but may be supported in Phase 2 which is under development
    public void testCardIndexNoIsAbsentInOutputWhenCardTokenRequiredIsFalseInOrder(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO order = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setCardTokenRequired(false)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        assertion.apply(responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not());

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NOT_PRESENT).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not());
    }

 /*   @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test(enabled = false) */
    //TODO disabling it because as per Nitin this feature is not supported for AddMoney as of now but may be supported in Phase 2 which is under development
    public void testCardIndexNoIsAbsentInOutputWhenCardTokenRequiredParamIsNotPassedInOrder(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO order = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .build();
        new CheckoutPage().createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        assertion.apply(responsePage.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not());

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NOT_PRESENT).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(com.paytm.pages.responsePage.ResponsePage.Attribute.CARD_INDEX_NO).not());
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCWalletCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }


    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 11.0;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

/*    @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 11.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        KYCPage kycPage = new KYCPage();
        Assert.assertTrue(kycPage.submit_btn().isElementPresent(),"KYC page is not getting opened for : " + user.mobNo());
    }
 /*   @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.0;
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .setGoodsInfo(goodsInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.0;
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .setGoodsInfo(goodsInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

 /*   @Test(enabled = false)
    @Override */
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
      //No need
    }
/*    @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.0;
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .setGoodsInfo(goodsInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 10001.0;
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .setGoodsInfo(goodsInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }
 /*   @Test(enabled = false)
    @Override */
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No need
    }
 /*   @Test(enabled = false)
    @Override */
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No need
    }


    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();

    }

    @Owner(DEEPAK)
    @Feature("PGP-22739")
    @Parameters({"theme"})
    @Test(description = "test pay mode is forwarded to Alipay in Payment Cashier Pay API for CC txn")
    public void testPayModeIsForwardedToAlipayInPaymentCashierPayForCCTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body("RESPCODE", Matchers.equalTo("01"));
        new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .executeUntilNotPending()
                .validateRespCode("01");
        String cmdToFetchSendOTPRequest = "grep '" + orderDTO.getORDER_ID() + "' /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest);
        Map map = (Map) new JsonSlurper().parseText(theiaFacadelogs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).as("").contains("\"paymentMode\":\"CC\"");
    }

    @Owner(DEEPAK)
    @Feature("PGP-22739")
    @Parameters({"theme"})
    @Test(description = "test pay mode is forwarded to Alipay in Payment Cashier Pay API for DC txn")
    public void testPayModeIsForwardedToAlipayInPaymentCashierPayForDCTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body("RESPCODE", Matchers.equalTo("01"));
        new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .executeUntilNotPending()
                .validateRespCode("01");
        String cmdToFetchSendOTPRequest = "grep '" + orderDTO.getORDER_ID() + "' /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest);
        Map map = (Map) new JsonSlurper().parseText(theiaFacadelogs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).as("").contains("\"paymentMode\":\"DC\"");
    }

    @Owner(GAURAV)
    @Feature("PGP-36144")
    @Parameters({"theme"})
    @Test(description = "Verify successful addMoney txn via saved VPA upto 2k")
    public void verifyAddMoneyviaSavedVPA2k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.UPICollectSavedVPA, theme, user).setTXN_AMOUNT("2000").build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isTrue();
        cashierPage.payBy(PayMode.SAVED_UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateTxnAmount("2000")
                .validateRespCode("01")
                .assertAll();
    }

    @Owner(GAURAV)
    @Feature("PGP-36144")
    @Parameters({"theme"})
    @Test(description = "Verify saved VPA and BHIM UPI not shown addMoney txn greater than 2k")
    public void verifyAddMoneyviaSavedVPAgreater2k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.UPICollectSavedVPA, theme, user).setTXN_AMOUNT("2010").build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isFalse();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("DC")
                .validateTxnAmount("2010")
                .validateRespCode("01")
                .assertAll();
    }
}
