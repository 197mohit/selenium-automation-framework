package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Deepak")
public class CCBillPayment extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate Successful CC bill payment via Net Banking")
    public void PGP_464_ccBillPaymentViaNB(@Optional("merchant4") String theme) throws Exception {
        String ccBillNo = null;
        System.out.println("trying to get lock");
        User user = userManager.getForWrite(Label.BASIC);
        System.out.println("got the lock");
        String custId = user.custId();
        String mid = MerchantType.PGOnly.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        ccBillNo = PGPHelpers.getCCBillPaymentToken(paymentDTO.getCreditCardNumber(), user.ssoToken());
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly, theme, user)
                .setTXN_AMOUNT("1.00")
                .setCC_BILL_NO(ccBillNo)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(PayMode.NB, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successful CC bill payment via Debit Card")
    public void PGP_465_ccBillPaymentViaDC(@Optional("enhancedweb") String theme) throws Exception {
        String ccBillNo = null;
        User user = userManager.getForRead(Label.BASIC);
        String custId = user.custId();
        String mid = MerchantType.PGOnly.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        ccBillNo = PGPHelpers.getCCBillPaymentToken(paymentDTO.getCreditCardNumber(), user.ssoToken());
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly, theme, user)
                .setTXN_AMOUNT("1.00")
                .setCC_BILL_NO(ccBillNo)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Single quotes in Response Message are Replaced With Blank Spaces")
    public void PGP_17782_ValidateSingleQuotesinRespMsgReplacedWithBlanks(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String displayMessage = (String) PGPHelpers.getResponseCodeMappingData(LocalConfig.PGP_DB_CONNECTION_URL,"371")
                .get(0).get("DISPLAY_MESSAGE");
        displayMessage = displayMessage.replace("'"," ");
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly, theme, user)
                .setTXN_AMOUNT("1.00")
                .setCC_BILL_NO("")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg(displayMessage)
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successful CC bill payment with PGOnly Login via Debit Card")
    public void validateCCBillPaymentUsingDC(@Optional("enchancedweb") String theme) throws Exception {
        String ccBillNo = null;
        User user = userManager.getForWrite(Label.BASIC);
        String custId = user.custId();
        String mid = MerchantType.PGOnly.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        ccBillNo = PGPHelpers.getCCBillPaymentToken(paymentDTO.getCreditCardNumber(), user.ssoToken());
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly, theme, user)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN(user.ssoToken())
                .setCC_BILL_NO(ccBillNo)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successful CC bill payment with PGOnly Login via Saved Card")
    public void validateCCBillPaymentUsingSavedCard(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String ccBillNo = null;
        User user = userManager.getForWrite(Label.BASIC);
        String custId = user.custId();
        String mid = MerchantType.PGOnly.getId();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        ccBillNo = PGPHelpers.getCCBillPaymentToken(paymentDTO.getCreditCardNumber(), user.ssoToken());
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly, theme, user)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN(user.ssoToken())
                .setCC_BILL_NO(ccBillNo)
                .build();   
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFCSC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successful CC bill payment via Retry Debit Card")
    public void validateCCBillPaymentUsingRetryDC(@Optional("enhancedweb") String theme) throws Exception {
        String ccBillNo = null;
        User user = userManager.getForRead(Label.BASIC);
        String custId = user.custId();
        String mid = MerchantType.PGOnly_Retry.getId();
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentDTO incorrectPaymentDTO = new PaymentDTO().setDebitCardNumber("4799475263852080");

        ccBillNo = PGPHelpers.getCCBillPaymentToken(paymentDTO.getCreditCardNumber(), user.ssoToken());
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN(user.ssoToken())
                .setCC_BILL_NO(ccBillNo)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(PayMode.DC,incorrectPaymentDTO);
        cashierPage.waitUntilLoads();

//        cashierPage.modalRetryPayment().accept();

        cashierPage.payBy(PayMode.DC,incorrectPaymentDTO);
        cashierPage.waitUntilLoads();

//        cashierPage.modalRetryPayment().accept();

        cashierPage.payBy(PayMode.DC,paymentDTO);
        cashierPage.waitUntilLoads();

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Successful CC bill payment via Retry Saved Card")
    public void validateCCBillPaymentUsingRetrySavedCard(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String ccBillNo = null;
        User user = userManager.getForWrite(Label.BASIC);
        String custId = user.custId();
        String mid = MerchantType.PGOnly_Retry.getId();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentDTO incorrectPaymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());
        SavedCardHelpers.addCard(user,incorrectPaymentDTO.getExpMonth(),incorrectPaymentDTO.getExpYear(),incorrectPaymentDTO.getCreditCardNumber());

        ccBillNo = PGPHelpers.getCCBillPaymentToken(paymentDTO.getCreditCardNumber(), user.ssoToken());
        OrderDTO orderDTO = new OrderFactory.CCBillPayment(MerchantType.PGOnly_Retry, theme, user)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN(user.ssoToken())
                .setCC_BILL_NO(ccBillNo)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,incorrectPaymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,incorrectPaymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();

        cashierPage.payBy(PayMode.SAVED_CARD,paymentDTO,paymentDTO.getDebitCardNumber());
        cashierPage.waitUntilLoads();

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFCSC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

}
