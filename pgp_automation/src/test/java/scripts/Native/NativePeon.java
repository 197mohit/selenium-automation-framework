package scripts.Native;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.DirectBankOTPPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Created by anjukumari on 15/11/18
 */

@Owner("Tarun")
public class NativePeon extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Peon send for Success Native CC transaction also validate", groups = "peon")
    public void TC_PT_001(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS").AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));        
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
             //   peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals("HDFC Bank"),
                peon.payMode().equals("CC"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters("isNativePlus")
    @Test(description = "Verify Peon send for Success Native Promo transaction also validate PROMO in peon response", groups = "peon")
    public void TC_PT_002(@Optional("false") Boolean isNativePlus) {
        ProcessTransactionTests processTransactionTests = new ProcessTransactionTests();
        OrderDTO orderDTO = processTransactionTests.initiateTxnUsingPromo(null, Constants.MerchantType.NATIVE_PROMO_PEON, Constants.promoCode.CC_PROMO, PayMethodType.CREDIT_CARD, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(responsePage.textTxnDate().getText()).isNotEmpty();
        processTransactionTests.validateSuccessPromo(processTransactionTests.fetchPaymentOptResponseDTO.get(), Constants.promoCode.CC_PROMO);
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
        //        peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "PROMO_CAMP_ID", "PROMO_RESPCODE", "PROMO_STATUS"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.promoCampId().equals(Constants.promoCode.CC_PROMO.toString()),
                peon.promoRespCode().equals("700"),
                peon.promoStatus().equals("PROMO_SUCCESS"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals("1.00"),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Peon send for Success Native Index token transaction also validate", groups = "peon")
    public void TC_PT_003(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID)
                .setCardTokenRequired("true").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS").AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
        //        peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals("HDFC Bank"),
                peon.payMode().equals("CC"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify hybrid success transaction, also validate Peon send")
    public void TC_PT_016(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.childTxnList().equals("").not(),
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
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Peon send when process txn is should be success when txn intiated using CC and retried using CC")
    public void verifyRetryCase_whenRetryIsDoneUsingCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID_RETRY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO order = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(order, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(order.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(order.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        OrderDTO retriedOrder = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(retriedOrder, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(retriedOrder.getMID(), retriedOrder.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(retriedOrder.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(retriedOrder.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(retriedOrder.getMID()),
                peon.orderId().equals(retriedOrder.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Peon send for success process txn using Direct channel (HDFC direct)")
    public void verifyRetryCase_InitiateCC_RetryUsingDirectChannel(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HDFO)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HDFO, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("123456");
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFO"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify PGonly Login success transaction using Saved Card, also validate Peon send")
    public void pgWithoutLoginTxnUsingSavedCard(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .setTxnValue("2.00")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify hybrid success transaction for EMI, also validate Peon send")
    public void hybridTxnUsingEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_EMI;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .setAUTH_MODE("OTP")
                .setPAYMENT_TYPE_ID("EMI")
                .setEMI_TYPE("CREDIT_CARD")
                .setPlanId("HDFC|1")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.childTxnList().equals("").not(),
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
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
        WalletHelpers.validateBalance(user, 0);
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Hybrid success transaction using Saved Card, also validate Peon send")
    public void hybridTxnUsingSavedCard(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .setTxnValue("2.00")
                .build();
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.childTxnList().equals("").not(),
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
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
        WalletHelpers.validateBalance(user, 0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful retry via EMI")
    public void retryTxnUsingEMI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Double txnAmount = 2.00;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID_RETRY)
                .setTxnValue(txnAmount.toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID_RETRY, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setAUTH_MODE("OTP")
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("HDFC|1")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        PaymentDTO newpaymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO retriedOrder = new OrderFactory.Native(Constants.MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, newpaymentDTO, PayMethodType.CREDIT_CARD)
                .setAUTH_MODE("OTP")
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("HDFC|1")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(retriedOrder, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(retriedOrder.getMID(), retriedOrder.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(retriedOrder.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(retriedOrder.getMID()),
                peon.orderId().equals(retriedOrder.getORDER_ID()),
                peon.payMode().equals("EMI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful retry using saved card")
    public void retryTxnUsingSavedCard(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID_RETRY;
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        String incorrectSaveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(incorrectSaveCardId);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String correctSaveCardId = SavedCardHelpers.getSavedCardId(user, 0);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");
        paymentDTO.setSavedCardId(correctSaveCardId);
        OrderDTO retriedOrder = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(retriedOrder, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(retriedOrder.getMID(), retriedOrder.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        com.paytm.utils.merchant.Peon peon = peons.getAt(retriedOrder.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(retriedOrder.getMID()),
                peon.orderId().equals(retriedOrder.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50859")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify CC peon/S2S data for DLF merchant in NQH")
    public void S2SDataForDLFMerchant_NQH_CC(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DLF_PEON_MERCHANT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.DLF_PEON_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        String orderID = orderDTO.getORDER_ID();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();

/*        String grepcmd = "sendS2SHighPriorityNotify";
        String nqhLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,grepcmd);
        String PGP_id = nqhLogs.substring(nqhLogs.indexOf("PGPID="), nqhLogs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);*/

      //  String commlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);
        String commlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,initTxnDTO.orderFromBody());
        Assertions.assertThat(commlogs).contains("\"peonServiceName\":\"dLFS2SPostService\"");
        Assertions.assertThat(commlogs).contains("\"type\":\"DLF_S2S_SERVICE\"");

    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50859")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify CC peon/S2S body for DLF merchant in CG")
    public void S2SBodyForDLF_CG_CC(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DLF_PEON_MERCHANT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.DLF_PEON_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        String orderID = orderDTO.getORDER_ID();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();

        String nqhLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,orderID, "S2SSyncPaymentPostService" );
        Assertions.assertThat(nqhLogs).contains(Constants.MerchantType.DLF_PEON_MERCHANT.getId());
        Assertions.assertThat(nqhLogs).contains("PaymentS2SBody in JSON format");
        Assertions.assertThat(nqhLogs).contains("\"STATUS\":\"TXN_SUCCESS\"");
        Assertions.assertThat(nqhLogs).contains("\"GATEWAYNAME\":\"HDFC\"");
        Assertions.assertThat(nqhLogs).contains("\"PAYMENTMODE\":\"CC\"");
        Assertions.assertThat(nqhLogs).contains("\"CURRENCY\":\"INR\"");
        Assertions.assertThat(nqhLogs).contains("\"BANKNAME\":\"HDFC Bank\"");
        Assertions.assertThat(nqhLogs).contains("\"RESPMSG\":\"Txn Success\"");
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50859")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify DC peon/S2S data for DLF merchant in NQH")
    public void S2SDataForDLFMerchant_NQH_DC(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DLF_PEON_MERCHANT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.DLF_PEON_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        String orderID = orderDTO.getORDER_ID();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();

/*        String grepcmd = "sendS2SHighPriorityNotify";
        String nqhLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,grepcmd);
        String PGP_id = nqhLogs.substring(nqhLogs.indexOf("PGPID="), nqhLogs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);*/

     //   String commlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);
        String commlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,initTxnDTO.orderFromBody());

        Assertions.assertThat(commlogs).contains("\"peonServiceName\":\"dLFS2SPostService\"");
        Assertions.assertThat(commlogs).contains("\"type\":\"DLF_S2S_SERVICE\"");

    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50859")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify DC peon/S2S body for DLF merchant in CG")
    public void S2SBodyForDLF_CG_DC(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DLF_PEON_MERCHANT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.DLF_PEON_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        String orderID = orderDTO.getORDER_ID();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();

        String nqhLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,orderID, "S2SSyncPaymentPostService" );
        Assertions.assertThat(nqhLogs).contains(Constants.MerchantType.DLF_PEON_MERCHANT.getId());
        Assertions.assertThat(nqhLogs).contains("PaymentS2SBody in JSON format");
        Assertions.assertThat(nqhLogs).contains("\"STATUS\":\"TXN_SUCCESS\"");
        Assertions.assertThat(nqhLogs).contains("\"GATEWAYNAME\":\"HDFC\"");
        Assertions.assertThat(nqhLogs).contains("\"PAYMENTMODE\":\"DC\"");
        Assertions.assertThat(nqhLogs).contains("\"CURRENCY\":\"INR\"");
        Assertions.assertThat(nqhLogs).contains("\"BANKNAME\":\"HDFC Bank\"");
        Assertions.assertThat(nqhLogs).contains("\"RESPMSG\":\"Txn Success\"");
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50859")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB peon/S2S data for DLF merchant in NQH")
    public void S2SDataForDLFMerchant_NQH_NB(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DLF_PEON_MERCHANT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.DLF_PEON_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        String orderID = orderDTO.getORDER_ID();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .assertAll();

/*        String grepcmd = "sendS2SHighPriorityNotify";
        String nqhLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler,grepcmd);
        String PGP_id = nqhLogs.substring(nqhLogs.indexOf("PGPID="), nqhLogs.indexOf("} -"));
        System.out.println("PGP_ID = " + PGP_id);*/

    //    String commlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,PGP_id);
        String commlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,initTxnDTO.orderFromBody());

        Assertions.assertThat(commlogs).contains("\"peonServiceName\":\"dLFS2SPostService\"");
        Assertions.assertThat(commlogs).contains("\"type\":\"DLF_S2S_SERVICE\"");

    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50859")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB peon/S2S body for DLF merchant in CG")
    public void S2SBodyForDLF_CG_NB(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DLF_PEON_MERCHANT).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.DLF_PEON_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        String orderID = orderDTO.getORDER_ID();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .assertAll();

        String nqhLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,orderID, "S2SSyncPaymentPostService" );
        Assertions.assertThat(nqhLogs).contains(Constants.MerchantType.DLF_PEON_MERCHANT.getId());
        Assertions.assertThat(nqhLogs).contains("PaymentS2SBody in JSON format");
        Assertions.assertThat(nqhLogs).contains("\"STATUS\":\"TXN_SUCCESS\"");
        Assertions.assertThat(nqhLogs).contains("\"GATEWAYNAME\":\"ICICI\"");
        Assertions.assertThat(nqhLogs).contains("\"PAYMENTMODE\":\"NB\"");
        Assertions.assertThat(nqhLogs).contains("\"CURRENCY\":\"INR\"");
        Assertions.assertThat(nqhLogs).contains("\"BANKNAME\":\"ICICI\"");
        Assertions.assertThat(nqhLogs).contains("\"RESPMSG\":\"Txn Success\"");

    }
}

