package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.RiskRejectHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Deepak")
public class TopUpExpress extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final RiskRejectHelper riskRejectHelper = new RiskRejectHelper();

    @Parameters({"theme"})
    @Test(description = "Validate successful topup express via CC.", groups = {"smoke"})
    public void PGP_407_successfulTopupXpressCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.TopupExpress(MerchantType.TopUpExpress_Onus, "CC", theme,user).build();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(MerchantType.TopUpExpress_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        SavedCardHelpers.validateSavedCardAbsence(user);
    }

    @Parameters({"theme"})
    @Test(description = "test txn < 1 not allowed")
    public void testTxnOfLessThan1NotAllowed(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchant = MerchantType.PG2_TopUpExpress_Onus;
        OrderDTO orderDTO = new OrderFactory.TopupExpress(merchant,"CC", theme,user)
                .setTXN_AMOUNT("0.99")
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateCheckSum(merchant.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .AssertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Parameters({"theme"})
    @Test(description = "verify Response message for Risk amount for CC TOP UP Express")
    public void verifyResponseMessageForRiskRejectTxnForCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchant = MerchantType.TopUpExpress_Onus;
        OrderDTO orderDTO = new OrderFactory.TopupExpress(merchant,"CC", theme,user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Parameters({"theme"})
    @Test(description = "verify Response message for Risk amount for DC TOP UP Express")
    public void verifyResponseMessageForRiskRejectTxnForDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchant = MerchantType.TopUpExpress_Onus;
        OrderDTO orderDTO = new OrderFactory.TopupExpress(merchant,"DC", theme,user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .build();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }
}
