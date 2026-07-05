package scripts.UI;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Epic(Constants.Sprint.SPRINT31_1)
@Feature("PGPUI-414")
@Feature("PGP-19943")
@Owner("Tarun")
public class PreLoginScreen extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    //Only for customer id's ending with zero
    private static final String custId = "12340";

    @BeforeClass
    public void UpdateSliderOTPtheme() {
        String queryForExp = "UPDATE FF4J_FEATURES SET EXPRESSION='percentage=10' WHERE FEAT_UID='preLoginTheme'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_URI).del("FF4J_FEATURE_preLoginTheme");
        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("FF4J_FEATURE_preLoginTheme");
    }


    //-------------------TestCases-----------------

    //Only for enhancedwap

    @Parameters("theme")
    @Test(description = "To verify for limited live merchants , pre login screen should contain Paytm Wallet,UPI and Payments Bank")
    public void preLoginInitial(@Optional("enhancedwap_revamp") String theme)
    {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.preLoginScreenElements())
                .as("Pre Login Screen is not correct")
                .containsExactly("Paytm Wallet","Paytm UPI","Paytm Payments Bank");


    }

    @Parameters("theme")
    @Test(description = "To test all the bhim upi handler on pre login screen")
    public void upiHandlerPreLogin(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI, theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("7988380094@");
        Assertions.assertThat(cashierPage.upiHandlers())
                .as("UPI handlers mismatch pre login")
                .containsSequence("@paytm@ybl@upi@oksbi@okhdfc@okicici@okaxis");
    }

    @Parameters("theme")
    @Test(description = "To test all the bhim upi handler on post login screen")
    public void upiHandlerPostLogin(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.directLogin(user);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("7988380094@");
        Assertions.assertThat(cashierPage.upiHandlers())
                .as("UPI handlers mismatch post login")
                .containsSequence("@paytm@ybl@upi@oksbi@okhdfc@okicici@okaxis");
    }

    @Parameters("theme")
    @Test(description = "To verify if DC is entered in CC text box pre login screen , then API success should be of DC")
    public void netBankingPreLogin(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Parameters("theme")
    @Test(description = "To verify for NB,only 4 bank icons should be displayed and 5th bank will replace 4th bank pre login screen")
    public void assertIconDisplayedinNBPreLoginScreen(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String bankName = "Yes Bank";  //Though Yes Bank is placed under moratorium by RBI :P
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        Assertions.assertThat(cashierPage.getNBIconCount()).as("Incorrect number of banks on NB screen").isEqualTo(8);
        cashierPage.selectOtherNetBanking(bankName);
        cashierPage.assertSelectedBank(bankName);

    }

    @Parameters("theme")
    @Test(description = "To verify for NB,only 4 bank icons should be displayed and 5th bank will replace 4th bank post login screen")
    public void assertIconDisplayedinNB(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String bankName ="Yes Bank";  //Though Yes Bank is placed under moratorium by RBI :P
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.directLogin(user);
        cashierPage.tabNetBanking().click();
        Assertions.assertThat(cashierPage.getNBIconCount()).as("Incorrect number of banks on NB screen").isEqualTo(8);
        cashierPage.selectOtherNetBanking(bankName);
        cashierPage.assertSelectedBank(bankName);

    }

}
