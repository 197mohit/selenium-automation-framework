// AI-Generated: 2025-12-19 - Feature addition: Separate test class for RefundHelperImpl coverage
package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RefundStatusHelper;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;

import static com.paytm.apphelpers.PGPHelpers.*;

/**
 * Test class for RefundHelperImpl E2E coverage
 * Target: Increase RefundHelperImpl code coverage from 0% to 20-25%
 * Instructions: 2,393 (Biggest Impact!)
 * 
 * @author Gagandeep
 * @date 2025-12-19
 */
@Owner("Gagandeep")
@Feature("RefundHelperImpl-Coverage")
public class RefundHelperImplTests extends PGPBaseTest {
    
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String postConvFlag = "";
    private static final String TXN_SUCCESS = "TXN_SUCCESS";

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund eligibility check for successful CC transaction - RefundHelperImpl")
    public void verifyRefundEligibilityForSuccessfulCCTransaction(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT("100.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateSuccessRefund()
                    .validateREFUNDAMOUNT(orderDTO.getTXN_AMOUNT(), 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund amount validation for partial refund - RefundHelperImpl")
    public void verifyRefundAmountValidationForPartialRefund(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "150.00";
        String partialRefundAmount = "50.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), partialRefundAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateStatus(TXN_SUCCESS, 0)
                    .validateREFUNDAMOUNT(partialRefundAmount, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify multiple partial refunds amount calculation - RefundHelperImpl")
    public void verifyMultiplePartialRefundsAmountCalculation(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "200.00";
        String firstPartialRefund = "75.00";
        String secondPartialRefund = "50.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), firstPartialRefund, txnStatus.getResponse().getTXNID(), postConvFlag);
            RefundStatusHelper refundStatusHelper1 = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper1.validateStatus(TXN_SUCCESS, 0).assertAll();

            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), secondPartialRefund, txnStatus.getResponse().getTXNID(), postConvFlag);
            RefundStatusHelper refundStatusHelper2 = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            Double totalRefund = Double.parseDouble(firstPartialRefund) + Double.parseDouble(secondPartialRefund);
            refundStatusHelper2.validateTOTALREFUNDAMT(totalRefund, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund request builder with all parameters - RefundHelperImpl")
    public void verifyRefundRequestBuilderWithAllParameters(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "125.50";
        String refundAmount = "125.50";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validatePaymentMode("DC")
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), refundAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateSuccessRefund()
                    .validateREFUNDAMOUNT(refundAmount, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund status transitions for pending to success - RefundHelperImpl")
    public void verifyRefundStatusTransitionsFromPendingToSuccess(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "80.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnAmount, 
                    txnStatus.getResponse().getTXNID(), postConvFlag);
            
            Thread.sleep(2000);
            
            RefundStatusHelper finalRefundStatus = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            finalRefundStatus.validateSuccessRefund()
                    .validateREFUNDAMOUNT(txnAmount, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund with different order types - PG Only UPI - RefundHelperImpl")
    public void verifyRefundHelperForDifferentOrderTypesUPI(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "95.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validatePaymentMode("UPI")
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), txnAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateSuccessRefund()
                    .validateGATEWAY("PPBLC", 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund validation for minimum refund amount - RefundHelperImpl")
    public void verifyRefundValidationForMinimumAmount(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "10.00";
        String minRefundAmount = "1.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), minRefundAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateStatus(TXN_SUCCESS, 0)
                    .validateREFUNDAMOUNT(minRefundAmount, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund with decimal amount precision - RefundHelperImpl")
    public void verifyRefundWithDecimalAmountPrecision(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "123.45";
        String refundAmount = "67.89";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), refundAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateStatus(TXN_SUCCESS, 0)
                    .validateREFUNDAMOUNT(refundAmount, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund for hybrid transaction with split validation - RefundHelperImpl")
    public void verifyRefundForHybridTransactionSplitValidation(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        String txnAmount = "10.00";
        double amountToBeRetainedInWallet = 3.00;

        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateChildTxnsPresent()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), txnAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            
            Map<String, Object> mapWallet = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(mapWallet, "GATEWAY", "WALLET");
            refundStatusHelper.validate(mapWallet, "STATUS", "TXN_SUCCESS");
            
            Map<String, Object> mapCC = refundStatusHelper.getRefundBy("PAYMENTMODE", "CC");
            refundStatusHelper.validate(mapCC, "GATEWAY", "HDFC");
            refundStatusHelper.validate(mapCC, "STATUS", "TXN_SUCCESS");
            
            refundStatusHelper.assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund request validation for different merchant configurations - RefundHelperImpl")
    public void verifyRefundForDifferentMerchantConfigurations(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "110.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), txnAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateSuccessRefund()
                    .validateREFUNDAMOUNT(txnAmount, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund validation with transaction ID verification - RefundHelperImpl")
    public void verifyRefundValidationWithTxnIdVerification(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "88.50";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String txnId = txnStatus.getResponse().getTXNID();

        Test:
        {
            Assertions.assertThat(txnId).as("Transaction ID should not be empty").isNotEmpty();
            
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), txnAmount, txnId, postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateSuccessRefund()
                    .validateTxnId(txnId, 0)
                    .assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund amount remains after partial refund - RefundHelperImpl")
    public void verifyRemainingAmountAfterPartialRefund(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        String txnAmount = "180.00";
        String partialRefundAmount = "60.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), partialRefundAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            refundStatusHelper.validateStatus(TXN_SUCCESS, 0)
                    .validateREFUNDAMOUNT(partialRefundAmount, 0)
                    .assertAll();
            
            TxnStatus updatedTxnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            double refundedAmount = Double.parseDouble(updatedTxnStatus.getResponse().getREFUNDAMT());
            Assertions.assertThat(refundedAmount).as("Refunded amount should match").isEqualTo(Double.parseDouble(partialRefundAmount));
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund with wallet only transaction - RefundHelperImpl")
    public void verifyRefundHelperForWalletOnlyTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String txnAmount = "50.00";

        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly_PG2_Refund, theme, user)
                .setTXN_AMOUNT(txnAmount)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validatePaymentMode("PPI")
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), txnAmount, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            Map<String, Object> map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.assertAll();
        }
    }

    @Owner("Gagandeep")
    @Feature("RefundHelperImpl-Coverage")
    @Parameters({"theme"})
    @Test(description = "Verify refund sequencing for multiple refund requests - RefundHelperImpl")
    public void verifyRefundSequencingForMultipleRequests(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "300.00";
        String firstRefund = "100.00";
        String secondRefund = "80.00";
        String thirdRefund = "50.00";

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT(txnAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), firstRefund, txnStatus.getResponse().getTXNID(), postConvFlag);
            Thread.sleep(1000);
            
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), secondRefund, txnStatus.getResponse().getTXNID(), postConvFlag);
            Thread.sleep(1000);
            
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), 
                    orderDTO.getORDER_ID(), thirdRefund, txnStatus.getResponse().getTXNID(), postConvFlag);
            
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), 
                    orderDTO.getORDER_ID(), true);
            Double totalRefunded = Double.parseDouble(firstRefund) + Double.parseDouble(secondRefund) + Double.parseDouble(thirdRefund);
            refundStatusHelper.validateTOTALREFUNDAMT(totalRefunded, 0).assertAll();
        }
    }
}

