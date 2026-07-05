package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Deepak")
public class SeamlessNative extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Test(description = "Validate Successful Seamless Native Txn with CC for Non-Existing User.")
    public void PGP_359_successfulSeamlessNativeCC() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SeamlessNative(MerchantType.SeamlessNative_Hybrid_Onus, "CC", user).build();
        checkoutPage.createOrder(orderDTO);
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
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate Successful Seamless Native Txn with DC for Non-Existing User.")
    public void PGP_360_successfulSeamlessNativeDC() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SeamlessNative(MerchantType.SeamlessNative_Hybrid_Onus, "DC", user).build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful Txn using Credit Card for Existing User without saving card.")
    public void PGP_361_SeamlessNativeCCWithoutSavedCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.SeamlessNative(MerchantType.SeamlessNative_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(SSOToken)
                .setSTORE_CARD("0")
                .build();
        checkoutPage.createOrder(orderDTO);
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
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        SavedCardHelpers.validateSavedCardAbsence(user);
    }

    @Test(description = "Validate Card is getting saved for using credit card for Existing user.")
    public void PGP_362_SeamlessNativeCCAndSavingCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.SeamlessNative(MerchantType.SeamlessNative_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(SSOToken)
                .setSTORE_CARD("1")
                .build();
        checkoutPage.createOrder(orderDTO);
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
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        SavedCardHelpers.validateSavedCardPresence(user);
    }

    @Test(description = "Validate Successful Seamless Txn with Saved Card for Existing User.")
    public void PGP_363_SeamlessNativeCCUsingSavedCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0));
        OrderDTO orderDTO = new OrderFactory.SeamlessNative(MerchantType.SeamlessNative_Hybrid_Onus, "CC", paymentDTO, user)
                .setSSO_TOKEN(SSOToken)
                .build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate Successful Seamless Native Txn with NB for Existing User.")
    public void PGP_364_successfulSeamlesNativeNB() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String SSOToken = user.ssoToken();
        OrderDTO orderDTO = new OrderFactory.SeamlessNative(MerchantType.SeamlessNative_Hybrid_Onus, "NB", user)
                .setSSO_TOKEN(SSOToken)
                .setBANK_CODE("ICICI")
                .build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

}
