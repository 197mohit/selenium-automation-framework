package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Sprint.SPRINT30_2;

@Owner("Gagandeep")
@Epic(SPRINT30_2)
@Feature("19129")
public class HybridPoenCheckSumValidation extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate peon checksum for Hybrid success transaction with s2s_checksum_with_all_param preference enabled")
    public void peonValidationHybridSuccessTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.Hybrid.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateCheckSum(Constants.MerchantType.Hybrid.getKey()).assertAll();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.Hybrid.getKey()));
                sAssert.eval();
    }




    @Parameters({"theme"})
    @Test(description = "Validate peon checksum for Hybrid failed transaction with s2s_checksum_with_all_param preference enabled")
    public void peonValidationHybridFailedTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.Hybrid.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("227"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.Hybrid.getKey()));
        sAssert.eval();
    }





    @Parameters({"theme"})
    @Test(description = "Validate peon checksum for Hybrid success transaction With CC as paymode and s2s_checksum_with_all_param preference enabled")
    public void peonValidationHybridSuccessTxnWithCC(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.Hybrid.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.Hybrid.getKey()));
        sAssert.eval();
    }





    @Parameters({"theme"})
    @Test(description = "Validate peon checksum for Hybrid failed transaction with DC paymode and s2s_checksum_with_all_param preference enabled")
    public void peonValidationHybridFailedTxnWithDC(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme,user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.DC_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.Hybrid.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("227"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.Hybrid.getKey()));
        sAssert.eval();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify peon checksum for Hybrid Success txn for native flow")
    public void peonValidationHybridSucessForNative(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= Constants.MerchantType.NATIVE_HYBRID;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(merchantType.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "CHILDTXNLIST", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(merchantType.getKey())

        );
        sAssert.eval();


    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that HYBRID txn failure for native flow")
    public void peonValidationHybridFailureForNative(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType= Constants.MerchantType.NATIVE_HYBRID;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("100.98")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(merchantType.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("227"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(merchantType.getKey()));
                sAssert.eval();


    }


    @Issue("20716")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Peon checksum when cardTokenRequired is set true in Initiate transaction for native txn", groups = Group.Status.TO_BE_FIXED)
    public void validatePeonChecksumCardTokenTrueNativeTxn(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user,1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setCardTokenRequired("true")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(merchantType.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "cardIndexNo","RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "CHILDTXNLIST", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(merchantType.getKey())

        );
        sAssert.eval();
    }


    @Issue("20716")
    @Parameters({"theme"})
    @Test(description = "Validate peon checksum for Hybrid success transaction having card index is true for enhance with s2s_checksum_with_all_param preference enabled", groups = Group.Status.TO_BE_FIXED)
    public void validatePeonWhenCardTokenisTrue(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        WalletHelpers.modifyBalance(user,1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme,user)
                .setCardTokenRequired(true)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCardIndexNo(Constants.ValidationType.NON_EMPTY).assertAll();
        responsePage.validateCheckSum(merchantType.getKey()).assertAll();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "cardIndexNo","RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(merchantType.getKey()));
        sAssert.eval();
    }
}