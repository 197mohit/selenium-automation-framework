package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.responsePage.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.pages.responsePage.ResponsePage.Attribute.SPLIT_SETTLEMENT_INFO;
import static com.paytm.pages.responsePage.ResponsePage.Attribute.STATUS;

@Owner("Tarun")
@Feature("PGP-24851")
public class SplitSettlements extends PGPBaseTest{

    private final static String vendor1 = "vendorid1";
    private final static String vendor2 = "vendorid2";
    private final ResponsePage responsePage = new ResponsePage();
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private void testPGOnlyOrderSuccessForSplitPaymentsByAmount(String theme, String payMode) {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setTXN_AMOUNT("2")
                .setSplitSettlementInfo(splitSettlementInfoByAmt(vendor1, 1, vendor2, 1))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        if ("NB".equals(payMode)) paymentDTO.setBankName("ICICI");
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDTO);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingCCForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByAmount(theme, "CC");
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingDCForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByAmount(theme, "DC");
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingNBForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByAmount(theme, "NB");
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingUPIForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByAmount(theme, "UPI");
    }

    private void testAddnPayOrderSuccessForSplitPaymentsByAmount(String theme, String payMode, double txnAmt, double walletBalance) throws Exception {
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_ADDNPAY;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(mainMerchant, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmt))
                .setSplitSettlementInfo(splitSettlementInfoByAmt(vendor1, 1, vendor2, 1))
                .build();
        WalletHelpers.modifyBalance(user, walletBalance);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        PaymentDTO paymentDTO = new PaymentDTO();
        if ("NB".equals(payMode)) paymentDTO.setBankName("ICICI");
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDTO);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessUsingCCForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) throws Exception {
        testAddnPayOrderSuccessForSplitPaymentsByAmount(theme, "CC", 2D, 1D);
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessUsingDCForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) throws Exception {
        testAddnPayOrderSuccessForSplitPaymentsByAmount(theme, "DC", 2D, 1D);
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessUsingNBForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) throws Exception {
        testAddnPayOrderSuccessForSplitPaymentsByAmount(theme, "NB", 2D, 1D);
    }

//    @Parameters({"theme"})
//    @Test(enabled = false)
    public void testAddnPayOrderSuccessUsingUPIForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) throws Exception {
        testAddnPayOrderSuccessForSplitPaymentsByAmount(theme, "UPI", 2D, 1D);
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessUsingPPIForSplitPaymentsByAmount(@Optional("enhancedweb") String theme) throws Exception {
        testAddnPayOrderSuccessForSplitPaymentsByAmount(theme, "WALLET", 2D, 2D);
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessForSplitPaymentsByAmountIncludingMainMerchant(@Optional("enhancedweb") String theme) {

        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setTXN_AMOUNT("2")
                .setSplitSettlementInfo(splitSettlementInfoByAmt(mainMerchant.getId(), 1, vendor1, 1))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessForSplitPaymentsByAmountIncludingMainMerchant(@Optional("enhancedweb") String theme) throws Exception {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_ADDNPAY;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(mainMerchant, theme, user)
                .setTXN_AMOUNT("2")
                .setSplitSettlementInfo(splitSettlementInfoByAmt(mainMerchant.getId(), 1, vendor1, 1))
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1D);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testOrderSuccessForSplitPaymentsByAmountWhenSplitAmtLessThanTxnAmt(@Optional("enhancedweb") String theme) {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setTXN_AMOUNT("3")
                .setSplitSettlementInfo(splitSettlementInfoByAmt(vendor1, 1, vendor2, 1))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    private void testPGOnlyOrderSuccessForSplitPaymentsByPercentage(String theme, String payMode) {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setSplitSettlementInfo(splitSettlementInfoByPercentage(vendor1, 30, vendor2, 70))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        if ("NB".equals(payMode)) paymentDTO.setBankName("ICICI");
        cashierPage.payBy(PayMode.valueOf(payMode), paymentDTO);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingCCForSplitPaymentsByPercentage(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByPercentage(theme, "CC");
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingDCForSplitPaymentsByPercentage(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByPercentage(theme, "DC");
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingNBForSplitPaymentsByPercentage(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByPercentage(theme, "NB");
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessUsingUPIForSplitPaymentsByPercentage(@Optional("enhancedweb") String theme) {
        testPGOnlyOrderSuccessForSplitPaymentsByPercentage(theme, "UPI");
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessForSplitPaymentsByPercentage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_ADDNPAY;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(mainMerchant, theme, user)
                .setTXN_AMOUNT("2")
                .setSplitSettlementInfo(splitSettlementInfoByPercentage(vendor1, 30, vendor2, 70))
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1D);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testPGOnlyOrderSuccessForSplitPaymentsByPercentageIncludingMainMerchant(@Optional("enhancedweb") String theme) {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setSplitSettlementInfo(splitSettlementInfoByPercentage(mainMerchant.getId(), 30, vendor1, 70))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testAddnPayOrderSuccessForSplitPaymentsByPercentageIncludingMainMerchant(@Optional("enhancedweb") String theme) throws Exception {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_ADDNPAY;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(mainMerchant, theme, user)
                .setTXN_AMOUNT("2")
                .setSplitSettlementInfo(splitSettlementInfoByPercentage(mainMerchant.getId(), 30, vendor1, 70))
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1D);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testOrderSuccessForSplitPaymentsByPercentageWhenSplitPercentageLessThan100(@Optional("enhancedweb") String theme) {
        FF4JFlags.enable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setSplitSettlementInfo(splitSettlementInfoByPercentage(vendor1, 30, vendor2, 60))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\"));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

//    @Parameters({"theme"}) //theia.addEscapeCharacterInFinalRespone FF4j flag is Enabled on Prod
//    @Test(enabled = false, description = "To test success txn for split settlement when ff4j flag (theia.addEscapeCharacterInFinalRespone) is OFF",priority = 1)
    public void testPGOnlyOrderSuccessForSplitPaymentsByAmountIncludingMainMerchantFF4jOff(@Optional("enhancedweb") String theme) {

        FF4JFlags.disable("theia.addEscapeCharacterInFinalRespone");
        Constants.MerchantType mainMerchant = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mainMerchant, theme)
                .setTXN_AMOUNT("2")
                .setSplitSettlementInfo(splitSettlementInfoByAmt(mainMerchant.getId(), 1, vendor1, 1))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        pageWait.apply(responsePage.hasLoaded());
        assertion.apply(responsePage.get(STATUS).equalsIgnoreCase("TXN_FAILURE").not());
        assertion.apply(responsePage.get(SPLIT_SETTLEMENT_INFO).contains("\\").not());

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }



    public String splitSettlementInfoByAmt(String mid1, double amt1, String mid2, double amt2) {
        return "{\"splitMethod\":\"AMOUNT\",\"splitInfo\":[{\"mid\":\"" + mid1 + "\",\"amount\":{\"value\":\"" + amt1 + "\",\"currency\":\"INR\"}},{\"mid\":\"" + mid2 + "\",\"amount\":{\"value\":\"" + amt2 + "\",\"currency\":\"INR\"}}]}";
    }

    public String splitSettlementInfoByPercentage(String mid1, double percentage1, String mid2, double percentage2) {
        return "{\"splitMethod\":\"PERCENTAGE\",\"splitInfo\":[{\"mid\":\"" + mid1 + "\",\"percentage\":\"" + percentage1 + "\"},{\"mid\":\"" + mid2 + "\",\"percentage\":\"" + percentage2 + "\"}]}";
    }

}
