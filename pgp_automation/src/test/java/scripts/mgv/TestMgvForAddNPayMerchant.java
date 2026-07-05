package scripts.mgv;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.base.test.UserManager;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.GiftVoucher;
import com.paytm.utils.merchant.intersections.MerchantUserIntersection;
import com.paytm.utils.merchant.merchant.util.Merchant;
import io.qameta.allure.Owner;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.MerchantType.*;

/**
 * @author rahul.kumar
 * @version $Id: TestMgvForAddNPayFlow.java, v 0.1 2020-01-28 12:52 rahul.kumar Exp $$
 */
@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Rahul Kumar")
public class TestMgvForAddNPayMerchant extends PGPBaseTest {

    private static final String TXN_SUCCESS = "TXN_SUCCESS";
    private static final String postConvFlag = "";

    private final CheckoutPage checkoutPage = new CheckoutPage();

    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (int i = 0; i < PaymodesOnPage.size(); i++) {
            paymethodList.add(PaymodesOnPage.get(i).getText().split("\n")[0]);
        }
        return paymethodList;
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful Txn with amount in whole number >= 1", groups = {"smoke"})
    public void successfulMgvOnlyTxn(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
       CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       if(cashierPage.checkBoxPPI().isChecked()){
           cashierPage.checkBoxPPI().unCheck();
       }
        cashierPage.payBy(Constants.PayMode.MGV);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                 .validateCheckSum(Constants.MerchantType.MGV_ADDNPAY.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify that MGV is getting displayed on Cashier Page when sso token is getting passed")
    public void testMgvIsGettingDisplayedOnCashierPage(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().unCheck();
        }
        cashierPage.giftVoucherHeader().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that Voucher Name is Same as Merchant Name")
    public void testVoucherNameisSameasMerchantName(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().unCheck();
        }
        cashierPage.giftVoucherHeader().assertVisible();
        Assert.assertTrue(cashierPage.giftVoucherHeader().getText().contains(cashierPage.MerchantName().getText()), "MGV name and Merchant name is not same");
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify MGV is coming on top of the page above Balance" , enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void testMgvIsComingOnTop(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertTrue(paymethodList.get(0).equalsIgnoreCase(cashierPage.MerchantName().getText() + " Voucher"));

    }

//    @Parameters({"theme"})
//    @Test(description = "Verify MGV is coming as default selected when user have Sufficient MGV and wallet amount" , enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void testMgvIsComingAsDefaultSelected(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        WalletHelpers.modifyBalance(user, 2.0);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.MGVradioButton().isSelected());

    }


    @Parameters({"theme"})
    @Test(description = "Verify MGV is coming as Disabled when user have insufficient Balance in it")
    public void testMgvIsComingAsDisabled(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().remove(new GiftVoucher(Balance - 1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).setTXN_AMOUNT("2.0")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().unCheck();
        Assert.assertFalse(cashierPage.giftVoucherHeader().isSelected());

    }


//    @Parameters({"theme"})
//    @Test(description = "Verify when user is getting login to the System and If he is having MGV it should be displayed", enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void testMGVPaymentAfterLoginToSystem(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.MGVradioButton().isSelected(), "MGV balance is insufficient but it is coming as selected");
    }

    @Parameters({"theme"})
    @Test(description = "Verify user can select any other payment option and MGV should get deselected")
    public void testSelectionForOtherPaymentMode(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().click();
        Assert.assertFalse(cashierPage.MGVradioButton().isSelected());

    }

    @Parameters({"theme"})
    @Test(description = "Verify if wallet having sufficient Balance and MGV also having sufficient balance MGV will comes selected default in this case")
    public void testwalletSufficientMGVSufficientMGVSelectedDefault(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.MGVradioButton().isEnabled());

    }

//    @Parameters({"theme"})
//    @Test(description = "Verify if wallet having insufficient Balance and MGV having sufficient balance MGV will comes selected default in this case" , enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void     testwalletInsufficientMGVSufficientMGVSelectedDefault(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        WalletHelpers.modifyBalance(user, 0.00);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.MGVradioButton().isEnabled());

    }

    @Parameters({"theme"})
    @Test(description = "Verify If Merchant is having MGV activated but user don't have")
    public void testMerchantHasMGVandUserDoesnot(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.NOMGV);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains(cashierPage.MerchantName().getText() + " Voucher"));
    }


    @Parameters({"theme"})
    @Test(description = "Verify if merchant don;t have MGV but user have enabled in it")
    public void testMerchantDonotHaveMGVandUserHave(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.AddnPay, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains(cashierPage.MerchantName().getText() + " Voucher"));
    }


    @Parameters({"theme"})
    @Test(description = "Verify successful refund of MGV transaction")
    public void successfulMGVRefund(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(5.00));

        prerequisite:
        {

            PGPHelpers.validateRefundAllowedWithChecksum(MGV_ADDNPAY.getId());
        }
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().unCheck();
        }
        cashierPage.payBy(Constants.PayMode.MGV);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund of MGV")
    public void successfulMGVPartialRefund(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(5.00));
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MGV_ADDNPAY.getId());
        }
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().unCheck();
        }
        cashierPage.payBy(Constants.PayMode.MGV);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), "1.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateTOTALREFUNDAMT("1.00", 0)
                    .validateREFUNDAMOUNT("1.00", 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "test if Multiple Voucher Redeem On User With Same Template Balance is Added and Displayed on Theia")
    public void testifMultipleVoucherRedeemOnUserWithSameTemplateBalanceisAdded(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double AddedBalance = mu.getGiftVouchers().getBalance();
        Assert.assertEquals(Balance + 1, AddedBalance, "Balance is not getting added");
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertEquals((Object) Double.valueOf(cashierPage.getGiftVoucherBalance().getText()), Balance + 1, "Balance is not getting added on theia");
    }

        @Parameters({"theme"})
        @Test(description = "Verify when Wallet is not enabled on Merchant but MGV is enabled then MGV Balance should be displayed")
        public void testMGVPaymentWhenWalletDisabledOnMerchant (@Optional("enhancedwap_revamp") String theme) throws Exception
        {
            User user = userManager.getForWrite(Label.LOGIN);
            Constants.MerchantType merchantType= MGV_WITHOUT_WALLET;
            com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
            Merchant m = new Merchant(merchantType.getId(),true);
            MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
            mu.getGiftVouchers().add(new GiftVoucher(2.00));
            OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_WITHOUT_WALLET, theme, user)
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            cashierPage.payBy(Constants.PayMode.MGV);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateCurrency("INR")
                    .validateMid(orderDTO.getMID())
                    .validateOrderId(orderDTO.getORDER_ID())
                    .validatePaymentMode("GIFT_VOUCHER")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                    .validateTxnDate(new Date())
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateCheckSum(Constants.MerchantType.MGV_WITHOUT_WALLET.getKey())
                    .assertAll();

        }

    @Parameters({"theme"})
    @Test(description = "Verify MGV is displayed when any other transaction is failed and Merchant has configured Retry for same")
    public void testInCaseOfRetryifTxnFailsMGVisPresent(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectcc = new PaymentDTO();
        incorrectcc.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, incorrectcc);
        cashierPage.waitUntilLoads();
        cashierPage.modalRetryPayment().accept();
        cashierPage.giftVoucherHeader().assertVisible();

    }


    @Parameters({"theme"})
    @Test(description = "Verify MGV successfull txn when any other transaction is failed and Merchant has configured Retry")
    public void testInCaseOfRetryifTxnFailsSuccessfulMGVTxn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectcc = new PaymentDTO();
        incorrectcc.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, incorrectcc);
        cashierPage.waitUntilLoads();
        cashierPage.modalRetryPayment().accept();
        if(cashierPage.checkBoxPPI().isChecked()){
        cashierPage.checkBoxPPI().unCheck(); }
        cashierPage.payBy(Constants.PayMode.MGV);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(MGV_ADDNPAY.getKey())
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify Retry through MGV is not possible when any other transaction is failed in first attempt and MGV has insufficient balance")
    public void testInCaseOfRetryifTxnFailsMGVhaveInsufficientBalance(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance =  mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().remove(new GiftVoucher(Balance - 1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_ADDNPAY, theme, user).setTXN_AMOUNT("2").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectcc = new PaymentDTO();
        incorrectcc.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, incorrectcc);
        cashierPage.waitUntilLoads();
        cashierPage.modalRetryPayment().accept();
        cashierPage.checkBoxPPI().unCheck();
        Assert.assertFalse(cashierPage.MGVradioButton().isSelected());
    }
    
    @Parameters({"theme"})
    @Test(description = "Verify if user have saved card on it He can do transaction via saved card")
    public void testWithSavedCard(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        PaymentDTO dto = new PaymentDTO();
        SavedCardHelpers.addCard(user, dto.getExpMonth(), dto.getExpYear(), dto.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.MGVradioButton().isSelected());
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.MGV_ADDNPAY.getKey())
                .assertAll();


    }


    @Parameters({"theme"})
    @Test(description = "test if Multiple Voucher Redeem On User With Different Template ID Balance is Not Added and single template balance Displayed on Theia")
    public void testifMultipleVoucherRedeemOnUserWithDifferentTemplateBalanceisNotAdded(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType1= MGV_ADDNPAY;
        Constants.MerchantType merchantType2 = MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m1 = new Merchant(merchantType1.getId(),true);
        MerchantUserIntersection mu1 = new MerchantUserIntersection(m1,u);
        mu1.getGiftVouchers().add(new GiftVoucher(1.00));
        double BalanceOnM1 =  mu1.getGiftVouchers().getBalance();
        Merchant m2 = new Merchant(merchantType2.getId(),true);
        MerchantUserIntersection mu2 = new MerchantUserIntersection(m2,u);
        mu2.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertEquals((Object) Double.valueOf(cashierPage.getGiftVoucherBalance().getText()),BalanceOnM1,"Balance is getting added on theia");

    }


    @Parameters({"theme"})
    @Test(description = "Test when user having zero MGV Balance then MGV not visible")
    public void testForMGVWithZeroBalanceADDNPAY(@Optional("enhancedwap") String theme) throws Exception{

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_ADDNPAY;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        Assert.assertTrue(mu.getGiftVouchers().add(new GiftVoucher(1.00)));
        double Balance = mu.getGiftVouchers().getBalance();
        Assert.assertTrue(mu.getGiftVouchers().remove(new GiftVoucher(Balance)));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_ADDNPAY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.giftVoucherHeader().assertNotVisible();

    }
}

