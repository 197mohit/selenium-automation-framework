package scripts.UI;

import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.base.test.Group;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.AJEESH;

@Owner("Gagandeep")
public class ZESTBANK  extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String SuccessAmount="2000";
    private static final String FailureAmount="2222";

    @Parameters({"theme"})
    @Test(description = "Verify Successful transaction with Peon for ZEST bank")
    public void SuccessfulTxnZestBank(@Optional("enhancedweb_revamp") String theme) throws Exception {

        ZestBankUI ui = new ZestBankUI();
        User user = userManager.getForRead(Label.RETRYPAYMODE);
        OrderDTO orderDTO = new OrderFactory.COD(Constants.MerchantType.ZEST_MONEY, theme, user)
                .setTXN_AMOUNT(SuccessAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        PaymentDTO zest = new PaymentDTO()
                .setBankName("ZEST");
        cashierPage.payBy(Constants.PayMode.ZEST, zest);
        ui.successZestTxn();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(Constants.MerchantType.ZEST_MONEY.getKey())
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateBankName("ZestMoney")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.validatePeon_EMIIndexToken(orderDTO,"ZEST","ZEST","TXN_SUCCESS");
    }

    @Parameters({"theme"})
    @Test(description = "Verify Failure transaction with Peon for ZEST bank")
    public void FailureTxnZestBank(@Optional("enhancedweb_revamp") String theme) throws Exception {

        ZestBankUI ui = new ZestBankUI();
        User user = userManager.getForRead(Label.RETRYPAYMODE);
        OrderDTO orderDTO = new OrderFactory.COD(Constants.MerchantType.ZEST_MONEY, theme, user)
                .setTXN_AMOUNT(FailureAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        PaymentDTO zest = new PaymentDTO()
                .setBankName("ZEST");
        cashierPage.payBy(Constants.PayMode.ZEST, zest);
        ui.successZestTxn();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(Constants.MerchantType.ZEST_MONEY.getKey())
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateBankName("ZestMoney")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();


        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.validatePeon_EMIIndexToken(orderDTO,"ZEST","ZEST","TXN_FAILURE");

    }
    @Owner(AJEESH)
    @Feature("PGP-33740")
    @Parameters({"theme"})
    @Test(description = "Verify Failure Transaction through Zest Money")
    public void Verify_failure_transaction_ZestMoney(@Optional("enhancedweb_revamp") String theme) throws Exception {
        ZestBankUI ui = new ZestBankUI();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ZEST_MONEY, theme)
                .setTXN_AMOUNT(FailureAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ZestMoney");
        cashierPage.payBy(Constants.PayMode.ZEST, paymentDTO);
        ui.failZestTxn();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("222")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(Constants.MerchantType.ZEST_MONEY.getKey())
                .assertAll();
    }

    @Owner(AJEESH)
    @Feature("PGP-33740")
    @Parameters({"theme"})
    @Test(description = "Verify Successful Transaction through Zest Money")
    public void Verify_successful_transaction_ZestMoney(@Optional("enhancedweb_revamp") String theme) throws Exception {
        ZestBankUI ui = new ZestBankUI();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ZEST_MONEY, theme)
                .setTXN_AMOUNT(SuccessAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ZestMoney");
        cashierPage.payBy(Constants.PayMode.ZEST, paymentDTO);
        ui.successZestTxn();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(Constants.MerchantType.ZEST_MONEY.getKey())
                .assertAll();
    }

}