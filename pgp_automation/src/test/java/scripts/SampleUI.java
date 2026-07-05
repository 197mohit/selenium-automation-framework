package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

public class SampleUI extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verify a successful COD transaction with zero wallet balance.", groups = {"smoke"})
    public void PGP_223_successfulCODTxnWithoutWalletBalance(@Optional("merchant4") String theme) throws Exception {
        /*Fetch basic user and apply write lock on it*/
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        /*Create COD order DTO*/
        OrderDTO orderDTO = new OrderFactory.COD(MerchantType.COD, theme, user)
                .setTXN_AMOUNT("11.00").build();
        /*Setting wallet balance to zero*/
        WalletHelpers.setZeroBalance(user);
        /*Initiate order*/
        checkoutPage.createOrder(orderDTO);
        /*Pay by COD pay mode*/
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.COD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("COD")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("CODMOCK")
                .validateBankName("CODMOCK")
                .validateCheckSum(MerchantType.COD.getKey())
                .validateResponsePageParameters()
                .assertAll();

        /*Excecuting TXNSTATUS API and validating response*/
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("CODMOCK")
                .validateBankName("CODMOCK")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("COD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
}
