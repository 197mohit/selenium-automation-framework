package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.dto.refund.ExtendInfo;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


/**
 * Created by ankuragarwal on 24/1/19
 */
@Owner("Gagandeep")
public class RefundStatus extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verify REFUND_STATUS response of PGOnly transaction")
    public void validateRefundStatusResp_PGOnly(@Optional("merchant4") String theme) throws PGPException {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();
        PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
        //TODO Added Temporary fix by adding pause need to handle it properly
        cashierPage.pause(45);
        PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                .validateSuccessRefund()
                .validateTxnId(txnStatus.getResponse().getTXNID(), 0)
                .validateOrderId(orderDTO.getORDER_ID(), 0)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT(), 0)
                .validateREFUNDAMOUNT(orderDTO.getTXN_AMOUNT(), 0)
                .validateTOTALREFUNDAMT(orderDTO.getTXN_AMOUNT(), 0)
                .validateREFID(orderDTO.getORDER_ID(), 0)
               .validateBANKTXNIDIsNotNull(0)
               .validateGATEWAY(txnStatus.getResponse().getGATEWAYNAME(), 0)
                .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                .assertAll();
    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-24411")
    @Parameters({"theme"})
    @Test(description = "Verify uniqueTxnId parameter(Numeric) is going in PGOnly Acquiring Refund request")
    public void verifyUniqueTxnId(@Optional("enhancedweb") String theme) throws PGPException, InterruptedException {
        Constants.MerchantType pgonlyMerchant = Constants.MerchantType.PGOnly_PG2_Refund;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(pgonlyMerchant.getId());
        }

        String uniqueTxnId = "1213133354";
        OrderDTO orderDTO = new OrderFactory.PGOnly(pgonlyMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();
        PGPHelpers.initiateAsyncRefundExtendInfo(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null,new ExtendInfo().setUniqueTxnId(uniqueTxnId));

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/refund_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_REFUND\" | grep \"REQUEST\"";
        String refundFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE,grepcmd);
        Assertions.assertThat(refundFacadeLogs).contains("uniqueTxnId").contains(uniqueTxnId);
    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-24411")
    @Parameters({"theme"})
    @Test(description = "Verify if unique txn id field value is alphanumeric is going in Acquiring Refund request")
    public void verifyUniqueTxnIdAlphaNumeric(@Optional("enhancedweb") String theme) throws PGPException, InterruptedException {
        Constants.MerchantType pgonlyMerchant = Constants.MerchantType.PGOnly_PG2_Refund;
        String uniqueTxnId = CommonHelpers.generateOrderId();

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(pgonlyMerchant.getId());
        }

        OrderDTO orderDTO = new OrderFactory.PGOnly(pgonlyMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();
        PGPHelpers.initiateAsyncRefundExtendInfo(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null,new ExtendInfo().setUniqueTxnId(uniqueTxnId));

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/refund_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_REFUND\" | grep \"REQUEST\"";
        String refundFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE,grepcmd);
        Assertions.assertThat(refundFacadeLogs).contains("uniqueTxnId").contains(uniqueTxnId);
    }



    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-24411")
    @Parameters({"theme"})
    @Test(description = "Verify AddNPay refund extra param")
    public void validateFullRefund(@Optional("enhancedweb") String theme) throws PGPException, InterruptedException {
        Constants.MerchantType pgonlyMerchant = Constants.MerchantType.AddnPay_PG2_Refund;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(pgonlyMerchant.getId());
        }

        OrderDTO orderDTO = new OrderFactory.PGOnly(pgonlyMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();
        PGPHelpers.initiateAsyncRefundExtendInfo(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null,new ExtendInfo().setUniqueTxnId("1213133354"));

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/refund_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_REFUND\" | grep \"REQUEST\"";
        String refundFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE,grepcmd);
        Assertions.assertThat(refundFacadeLogs).contains("uniqueTxnId").contains("1213133354");
    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-24411")
    @Parameters({"theme"})
    @Test(description = "Verify Hybrid refund extra param")
    public void validateHybridRefund(@Optional("enhancedweb") String theme) throws PGPException, InterruptedException {
        Constants.MerchantType pgonlyMerchant = Constants.MerchantType.Hybrid;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(pgonlyMerchant.getId());
        }

        OrderDTO orderDTO = new OrderFactory.PGOnly(pgonlyMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();
        PGPHelpers.initiateAsyncRefundExtendInfo(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null,new ExtendInfo().setUniqueTxnId("1213133354"));

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/refund_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_REFUND\" | grep \"REQUEST\"";
        String refundFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE,grepcmd);
        Assertions.assertThat(refundFacadeLogs).contains("uniqueTxnId").contains("1213133354");;
    }



}
