package scripts.Native.Khata;

import com.paytm.RefundSucessNotifyPeon;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RefundStatusV1Helper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


@Owner("Tarun")
@Epic(Constants.Sprint.SPRINT31_1)
@Feature("PGP-19819")
public class KhataRefund extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();

    public void validateS2SPreference(Constants.MerchantType merchantType) {
        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "S2S_CHARGEBACK_PEON_ENABLED", "Y");
        }
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that when S2S peon is enabled on the khatabook child merchant but the peon url is not set then peon is not sent to the merchant")
    public void peonNotSentToMerchant(@Optional("false") Boolean isNativePlus)
    {
        validateS2SPreference(Constants.MerchantType.PGOnly_PG2_Refund);
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_PG2_Refund)
                .setTxnValue(txnAmount)
                .setMerchantKey(Constants.MerchantType.PGOnly_PG2_Refund.getKey())
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly_PG2_Refund, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(merchantType.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();

        RefundSucessNotifyPeon refundNotify = new RefundSucessNotifyPeon(orderDTO.getORDER_ID(), Constants.MerchantType.ADVANCE_DEPOSIT.getId());
        refundNotify.executeToGetNoResponse();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify that when S2S peon is enabled on the khatabook child merchant but the peon url is not set then peon is not sent to the merchant")
    public void peonNotSentToMerchant1(@Optional("false") Boolean isNativePlus)
    {
        validateS2SPreference(Constants.MerchantType.PGOnly_PG2_Refund);
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_PG2_Refund)
                .setTxnValue(txnAmount)
                .setMerchantKey(Constants.MerchantType.PGOnly_PG2_Refund.getKey())
                //.setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly_PG2_Refund, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(merchantType.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();

        RefundSucessNotifyPeon refundNotify = new RefundSucessNotifyPeon(orderDTO.getORDER_ID(), Constants.MerchantType.ADVANCE_DEPOSIT.getId());
        refundNotify.executeToGetNoResponse();
    }

    //TODO Debugging with Ankit
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that when S2S peon is enabled on the khatabook aggregator merchant and the peon url is set then the refund peon should be generated and sent on the both child and parent and signature should be non empty")
    public void peonShouldSentToMerchant(@Optional("false") Boolean isNativePlus)
    {
        Constants.MerchantType parentMID = Constants.MerchantType.ADVANCE_DEPOSIT;
        Constants.MerchantType childMID = Constants.MerchantType.PGOnly_PG2_Refund;

        validateS2SPreference(parentMID);
        validateS2SPreference(childMID);
        String txnAmount = "2.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, childMID)
                .setTxnValue(txnAmount)
                .setMerchantKey(childMID.getKey())
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(parentMID.getId())
                .build(parentMID.getKey());

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(childMID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(parentMID.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String refId = String.valueOf(CommonHelpers.getRandomWithSize(6));
        PGPHelpers.initiateAsyncRefundWithAggMID(orderDTO.getMID(),orderDTO.getAggrMid(),childMID.getKey(),
                orderDTO.getORDER_ID(),refId , txnStatus.getResponse().getTXNID(), txnAmount,
                "REFUND", "Initiate Refund", null);

        PGPHelpers.getRefundStatusV1(childMID, orderDTO.getORDER_ID(), refId, true)
                .validateSuccessRefund()
                .validateMid(orderDTO.getMID())
                .validateRefundAmount(txnAmount)
                .validateTotalRefundAmount(txnAmount)
                .validateRefundDetailInfoList(RefundStatusV1Helper.PAY_METHODS.DEBIT_CARD, txnAmount, "HDFC")
                .asserAll();


        //Refund Success Notify should be sent to parent MID
        RefundSucessNotifyPeon parentNotify = new RefundSucessNotifyPeon(orderDTO.getORDER_ID(),parentMID.getId());
        parentNotify.validateBasicDetails(parentMID,orderDTO);

        //Refund Success Notify should be sent to child MID
        RefundSucessNotifyPeon childNotify = new RefundSucessNotifyPeon(orderDTO.getORDER_ID(), childMID.getId());
        childNotify.validateBasicDetails(childMID,orderDTO);

    }


}
