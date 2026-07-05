package scripts.mgv;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.GiftVoucher;
import com.paytm.utils.merchant.Peon;
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

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Rahul Kumar")
public class TestMGVForHybridMerchant extends PGPBaseTest {
    private static final String postConvFlag = "";
    private static final String TXN_SUCCESS = "TXN_SUCCESS";

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
    @Test(description = "Validate successful Txn with amount in whole number >= 1 also validate its checksums and peon")
    public void successfulMgvOnlyTxn(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
                .validateCheckSum(Constants.MerchantType.MGV_HYBRID.getKey())
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
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals(""),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals(""),
                peon.payMode().equals("GIFT_VOUCHER"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),//PGP-21858
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();

    }

    @Parameters({"theme"})
    @Test(description = "Verify that MGV is getting displayed on Cashier Page when sso token is getting passed")
    public void testMgvDisplayOnCashierPage(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage= CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.giftVoucherHeader().assertVisible();
    }


    @Parameters({"theme"})
    @Test(description = "Verify that Voucher Name is Same as Merchant Name")
    public void testVoucherNameisSameasMerchantName(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.giftVoucherHeader().assertVisible();
        Assert.assertTrue(cashierPage.giftVoucherHeader().getText().contains(cashierPage.MerchantName().getText()), "MGV name and Merchant name is not same");
    }

    @Parameters({"theme"})
    @Test(description = "Verify MGV disabled When Txn Amount is Greater than MGV balance")

    public void testVoucherisDisabledifTxnAmountisGreaterThanMGVBalance(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().remove(new GiftVoucher(Balance-1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user)
                .setTXN_AMOUNT("2").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertFalse(cashierPage.MGVradioButton().isEnabled(), "MGV section is not disabled when txn balance > MGV balance");
    }

    @Parameters({"theme"})
    @Test(description = "Verify user can select any other payment option and MGV should get deselected")
    public void testUserpayOtherPaymodeMGVshouldBeDeselected(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.MGVradioButton().isEnabled());
        cashierPage.tabCreditCard().click();
        Assert.assertFalse(cashierPage.MGVradioButton().isSelected());
    }

    @Parameters({"theme"})
    @Test(description = "Verify If Merchant is having MGV activated but user don't have")
    public void testMerchantHasMGVandUserDoesnot(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.NOMGV);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains(cashierPage.MerchantName().getText() + " Voucher"));
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify MGV is coming as default selected when user have Sufficient MGV Balance", enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void testMGVSelectedByDefaultForSufficientBalance(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assert.assertTrue(cashierPage.MGVradioButton().isSelected());

    }


//    @Parameters({"theme"})
//    @Test(description = "Verify MGV is coming on TOP of All Paymodes", enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void testMGVisTOPofAllPaymode(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertTrue(paymethodList.get(0).contains("Voucher"));
    }


    @Parameters({"theme"})
    @Test(description = "Verify If User is having MGV activated but Merchant don't have")
    public void testUserHasMGVandMerchantDoesnot(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.PGOnly, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains(cashierPage.MerchantName().getText() + " Voucher"));
    }


//    @Parameters({"theme"})
//    @Test(description = "Verify when user is getting login to the System and If he is having MGV it should be displayed", enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void testMGVPaymentAfterLoginToSystem(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN,Label.MGV);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        Assert.assertTrue(cashierPage.MGVradioButton().isSelected(), "User having MGV Not getting displayed");
    }
    @Parameters({"theme"})
    @Test(description = "Verify MGV is displayed when any other transaction is failed and Merchant has configured Retry for same")
    public void testInCaseOfRetryifTxnFailsMGVisPresent(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
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
    public void testInCaseOfRetryifTxnFailsSuccessfulMGVTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MGV);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage= CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectcc = new PaymentDTO();
        incorrectcc.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, incorrectcc);
        cashierPage.waitUntilLoads();
        cashierPage.modalRetryPayment().accept();
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
                .validateCheckSum(MGV_HYBRID.getKey())
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Validate successful Txn with amount not in whole number >= 1")
    public void successfulMgvOnlyTxnWithDoubleAmount(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(5.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user)
                .setTXN_AMOUNT("2.42")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
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
                .validateCheckSum(MGV_HYBRID.getKey())
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify user can select any other payment option and MGV should get deselected")
    public void testSelectionForOtherPaymentMode(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MGV);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        WalletHelpers.modifyBalance(user,10.0);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        Assert.assertFalse(cashierPage.MGVradioButton().isSelected());
        cashierPage.payBy(Constants.PayMode.WALLET);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of MGV transaction")
    public void successfulMGVRefund(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(5.00));

        prerequisite:
        {

            PGPHelpers.validateRefundAllowedWithChecksum(MGV_HYBRID.getId());
        }
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.MGV);
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
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(5.00));
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MGV_HYBRID.getId());
        }
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MGV_HYBRID, theme,user)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.MGV);
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
    @Test(description = "Verify if user have saved card on it He can do transaction via saved card")
    public void testWithSavedCard(@Optional("enhancedweb") String theme) throws Exception{
        // TODO :- Replace user in future with a basic user
        User user = userManager.getForWrite(Label.BASIC,Label.PPBL);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(5.00));
        PaymentDTO dto = new PaymentDTO();
        SavedCardHelpers.addCard(user, dto.getExpMonth(), dto.getExpYear(), dto.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.MGVradioButton().isSelected()==false){
            cashierPage.MGVradioButton().click();
        }else {
            Assert.assertTrue(cashierPage.MGVradioButton().isSelected());
        }
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
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
                .validateCheckSum(MGV_HYBRID.getKey())
                .assertAll();


    }


    @Parameters({"theme"})
    @Test(description = "For Aggregator merchant main merchant name is visible")
    public void testForAggregatorMerchant(@Optional("enhancedweb") String theme) throws Exception{


        User user = userManager.getForWrite(Label.MGV);
        Constants.MerchantType merchantType= MGV_AGGREGATOR;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_AGGREGATOR_CHILD, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage= CashierPageFactory.getCashierPage(theme);
        if(cashierPage.MGVradioButton().isSelected()==false){
            cashierPage.MGVradioButton().click();
        }else {
            Assert.assertTrue(cashierPage.MGVradioButton().isSelected());
        }
        Assert.assertFalse(cashierPage.giftVoucherHeader().getText().contains(cashierPage.MerchantName().getText()), "MGV name and Merchant name is not same");

    }


    @Parameters({"theme"})
    @Test(description = "Test when user having zero MGV Balance then MGV not visible")
    public void testForMGVWithZeroBalance(@Optional("enhancedweb") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= MGV_HYBRID;
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(),true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m,u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().remove(new GiftVoucher(Balance));
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.MGV_HYBRID, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.giftVoucherHeader().assertNotVisible();

    }

}
