package scripts.Sprint28;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Epic(Constants.Sprint.SPRINT28_2)
@Feature("PGP-15980")
@Owner("Tarun")
public class OrderRetryImproveSuccessRate extends PGPBaseTest{

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final double txnAmount = 1.00;

    // select * from RESPONSE_CODE_MAPPING where PAYTM_RESPCODE = '235'
    //Wallet Balance insufficient

    @Parameters({"theme"})
    @Test(description = "To verify if there is retry for a non retriable scenario if merchant retry is possible")
    public void verifyRetryAllowedForNonRetriableResponseCode(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid_Retry, theme).setTXN_AMOUNT(String.valueOf(txnAmount)).
                setSSO_TOKEN(user.ssoToken()).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user); //setting wallet balance to zero at runtime so that RESULT_CODE becomes BALANCE_NOT_ENOUGH at the time of payment
        cashierPage.payBy(Constants.PayMode.WALLET);
        cashierPage.waitUntilLoads();

        cashierPage.payBy(Constants.PayMode.NB,paymentDTO.setBankName("ICICI"));
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
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();


}


    @Parameters({"isNativePlus"})
    @Test(description = "NATIVE - To verify if there is retry for a non retriable scenario if merchant retry is possible ")
    public void nativeVerifyRetryAllowedForNonRetriableResponseCode(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType  = Constants.MerchantType.NATIVE_HYBRID_RETRY;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue(String.valueOf(txnAmount)).build();
        String txnToken  = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(String.valueOf(txnAmount))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    //@Issue("PGP-20120")
    @Parameters({"theme"})
    @Test(description = "To verify if there is no retry when non retriable response is received and merchant retry is exhausted")
    public void verifyNoRetryIfCountExhausted(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid_Retry, theme).setTXN_AMOUNT(String.valueOf(txnAmount)).
                setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user); //setting wallet balance to zero at runtime so that RESULT_CODE becomes BALANCE_NOT_ENOUGH at the time of payment
        cashierPage.payBy(Constants.PayMode.WALLET);
        cashierPage.waitUntilLoads();

        cashierPage.payBy(Constants.PayMode.WALLET);
        cashierPage.waitUntilLoads();

        cashierPage.payBy(Constants.PayMode.WALLET);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")//Getting system error , raised bug
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "NATIVE - To verify if there is no retry when non retriable response is received and merchant retry is exhausted")
    public void nativeVerifyNoRetryIfCountExhausted(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType  = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue(String.valueOf(txnAmount)).build();
        String txnToken  = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.BALANCE)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");


        orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.BALANCE)
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");


    }

    @Parameters({"theme"})
    @Test(description = "To verify if there is no retry when retriable response is received and merchant retry is exhausted")
    public void verifyNoRetryIfCountExhaustedAndRetriableResponse(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN).setDebitCardNumber(PaymentDTO.DC_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid_Retry, theme).setTXN_AMOUNT(String.valueOf(txnAmount)).
                setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.waitUntilLoads();
        if(theme.contains("enhanced")) {
            cashierPage.modalRetryPayment().accept();
        }

        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);
        cashierPage.waitUntilLoads();
        if(theme.contains("enhanced")) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "NATIVE - To verify if there is no retry when retriable response is received and merchant retry is exhausted")
    public void nativeVerifyNoRetryIfCountExhaustedAndRetriableResponse(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType  = Constants.MerchantType.STORE_CASH;
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN).setDebitCardNumber(PaymentDTO.DC_FAILED_TXN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue(String.valueOf(txnAmount)).build();
        String txnToken  = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

//        orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
//                .build();
//        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
//        responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
//        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
//        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");


        orderDTO = new OrderFactory.Native(Constants.MerchantType.STORE_CASH, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

    }

    /*@Parameters({"theme"})
    @Test(description = "To verify if there is no retry when non retriable response is received and merchant retry is 0",enabled = false)
    public void verifyNoRetryIfCountisZero(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
       txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }*/

    /*@Parameters({"isNativePlus"})
    @Test(description = "Native - To verify if there is no retry when non retriable response is received and merchant retry is 0",enabled = false)
    //wallet is not supported
    public void nativeVerifyNoRetryIfCountisZero(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType merchantType  = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue(String.valueOf(txnAmount)).build();
        String txnToken  = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(String.valueOf(txnAmount))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();


    }
*/

}
