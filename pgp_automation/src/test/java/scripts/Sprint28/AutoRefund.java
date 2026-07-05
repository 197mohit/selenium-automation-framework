package scripts.Sprint28;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.AutoRefundPeon;
import com.paytm.api.SMSPrimary;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.CloseOrder;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

// FundBack and Revoke auto refund cases

//PGP-14676
@Owner("Tarun")
public class AutoRefund extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;  //AUTO_REFUND_PEON_ENABLED (Active)
    private static final double txnAmount = 5.0;
    private static final double amountToBeRetainedInWallet = 2.0;

    private void isAutoRefundEnabled(Constants.MerchantType merchantType)
    {
        prerequisite:{
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_REFUND_PEON_ENABLED", "Y");
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "AUTO_REFUND_PEON_URL", "Y");
                   }
    }

    private void autoRefundSmsHelper(String ORDER_ID, String user) {
        SMSPrimary smsPrimary = new SMSPrimary(ORDER_ID);
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo")).as("SMS is going to wrong mobile number").contains(user);
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("message")).as("Refund message is incorrect").contains(ORDER_ID.substring(5));

    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of REVOKE of PG mode CC")
    public void revokeCCPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {

        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        cashierPage.payBy(Constants.PayMode.CC);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount-amountToBeRetainedInWallet,
                Constants.Bank.HDFCBANK.toString(),"CREDIT_CARD");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of REVOKE of PG mode DC")
    public void revokeDCPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);

        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO, txnAmount - amountToBeRetainedInWallet,
                Constants.Bank.HDFCBANK.toString(), "DEBIT_CARD");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of REVOKE of PG mode NB")
    public void revokeNBPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName(Constants.Bank.ICICI.toString());
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount-amountToBeRetainedInWallet,
                Constants.Gateway.ICICO.toString(),"NET_BANKING");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "NB")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.ICICI.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.ICICI.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of REVOKE of PG mode Paytm CC")
    public void revokePaytmCCPeon(@Optional("enhancedweb") String theme) throws Exception {

        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.POSTPAID);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        PostpaidHelpers.updateBalance(String.valueOf(txnAmount-amountToBeRetainedInWallet));
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid,theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);

        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount-amountToBeRetainedInWallet,
                Constants.Gateway.PAYTMCC.toString(),"PAYTM_DIGITAL_CREDIT");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "Paytm Postpaid")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.PAYTMCC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of REVOKE of PG mode UPI Collect")
    public void revokeUPICollectPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {

        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.UPI,paymentDTO);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount-amountToBeRetainedInWallet,
               null,"UPI");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "UPI")
                .validateGatewayName(TxnStatus.ChildTxnType.BANK,Constants.Gateway.ICICI.toString())
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }


    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of REVOKE of PG mode PPBL")
    public void revokePPBLPeon(@Optional("enhancedweb") String theme) throws Exception {

        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.PPBL,Label.BASIC);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        cashierPage.payBy(Constants.PayMode.PPBL);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount-amountToBeRetainedInWallet,
                "Paytm Payments Bank","NET_BANKING");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "NB")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.PPBL.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.PPBL.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_FAILURE");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_FAILURE")
                .AssertAll();

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description = "Verify the refund peon in case of REVOKE of PG mode UPI Intent")
    public void refundUPIIntentPeon(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_INTENT;
        isAutoRefundEnabled(merchantType);
        User user  = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setRequestType("NATIVE")
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme,user)
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO =
                NativeHelpers.fetchPaymentOptionResponse(txnToken, merchantType.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, PayMethodType.UPI.toString(), false))
                .as(PayMethodType.UPI.toString() + " paymethod status mismatched")
                .isTrue();
        WalletHelpers.setZeroBalance(user);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("UPI_INTENT")
                .setPaymentFlow("HYBRID")
                .build();

        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Map<String, String> map = PGPHelpers.parseUpiIntentDeepLink(processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink());
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(String.valueOf(txnAmount-amountToBeRetainedInWallet))
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(initTxnDTO.orderFromBody())
                .setExternalSerialNo(map.get("tr"))
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchantType.getId());
        Response response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(initTxnDTO.orderFromBody());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount-amountToBeRetainedInWallet,null,"UPI");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    //-------------------------------FUNDBACK-------------------------------------


    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode CC")
    public void fundBackCCPeon(@Optional("enhancedweb") String theme) throws Exception {
        double txnAmount = 67.0;
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PG2_Refund;
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.fillAndSubmitCCDetails(new PaymentDTO(), false);

        //Closing order to create a fundback refund

        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();
        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                Constants.Bank.HDFCBANK.toString(),"CREDIT_CARD");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode DC")
    public void fundBackDCPeon(@Optional("enhancedweb") String theme) throws Exception {
        double txnAmount = 67.0;
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PG2_Refund;

        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.fillAndSubmitDCDetails(new PaymentDTO(), false);

        //Closing order to create a fundback refund

        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                Constants.Bank.HDFCBANK.toString(),"DEBIT_CARD");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }



    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode EMI")
    public void fundBackEMIPeon(@Optional("enhancedweb") String theme) throws Exception {
        double txnAmount = 67.0;

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PG2_Refund;
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName(Constants.Bank.HDFCBANK.toString());
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.fillAndSubmitEMIDetails(paymentDTO);

        //Closing order to create a fundback refund

        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                Constants.Bank.HDFCBANK.toString(),"EMI");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Issue("SMP1-4840")
    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode Paytm CC",groups = Group.Status.BUG)
    public void fundBackPaytmCCPeon(@Optional("enhancedweb") String theme) throws Exception {
        double txnAmount = 67.0;
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.POSTPAID);
        PostpaidHelpers.updateBalance(String.valueOf(txnAmount));

        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        //Closing order to create a fundback refund
        Thread.sleep(2000);
        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                Constants.Bank.HDFCBANK.toString(),"PAYTM_DIGITAL_CARD");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode UPI Collect")
    public void fundBackUPICollectPeon(@Optional("enhancedweb") String theme) throws Exception {
        double txnAmount = 67.0d;
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_UPI_COLLECT;
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        //Closing order to create a fundback refund

        Thread.sleep(3000);
        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                Constants.Bank.HDFCBANK.toString(),"UPI");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode PPBL")
    public void fundBackPPBL(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PG2_Refund;
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.fillAndSubmitPPBLDetail(new PaymentDTO());

        //waiting for doPayment call
        Thread.sleep(1000);

        //Closing order to create a fundback refund
        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                "Paytm Payments Bank","NET_BANKING");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PG mode Wallet")
    public void fundBackBalance(@Optional("enhancedweb") String theme) throws Exception {
        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.fillAndSubmitWalletDetails(new PaymentDTO());

        //Closing order to create a fundback refund

        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackOrRevokeTxn(orderDTO,txnAmount,
                null,"BALANCE");

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of hybrid FUNDBACK of both paymodes")
    public void fundBackHybrid(@Optional("enhancedweb") String theme) throws Exception {
        double txnAmount = 69.0;
        double amountToBeRetainedInWallet = 2.0;

        isAutoRefundEnabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType,theme,user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.fillAndSubmitCCDetails(paymentDTO, false);

        //Closing order to create a fundback refund

        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validateFundBackHybridTxn(orderDTO,txnAmount,
                Constants.Bank.HDFCBANK.toString(),"CREDIT_CARD",amountToBeRetainedInWallet);

        autoRefundSmsHelper(orderDTO.getORDER_ID(), user.mobNo());
    }

    @Epic("PWP")
    @Feature("PGP-20557")
    @Parameters({"theme"})
    @Test(description =  "Verify the refund revoke auto refund peon in case of PWP Credit Card")
    public void revokeCCPWPPeon(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType pwpHybrid = Constants.MerchantType.PWP_HYBRID;
        isAutoRefundEnabled(pwpHybrid);
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,amountToBeRetainedInWallet);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybrid,theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.setZeroBalance(user);
        cashierPage.payBy(Constants.PayMode.CC);

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validatePWPTxn(orderDTO,txnAmount-amountToBeRetainedInWallet);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("235")
                .validateRespMsg("Wallet balance Insufficient")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PAYTM")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

    }


    @Epic("PWP")
    @Feature("PGP-20557")
    @Parameters({"theme"})
    @Test(description =  "Verify the refund peon in case of FUNDBACK of PWP mode DC")
    public void fundBackDCPeonPWP(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txnAmount = 67.0;
        Constants.MerchantType pwpHybrid = Constants.MerchantType.PWP_HYBRID;

        isAutoRefundEnabled(pwpHybrid);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(pwpHybrid,theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.fillAndSubmitDCDetails(new PaymentDTO(), false);

        //Closing order to create a fundback refund

        CloseOrder closeOrder = new CloseOrder(orderDTO.getORDER_ID(),orderDTO.getMID(),user.ssoToken());
        JsonPath response =  closeOrder.execute().jsonPath();

        Assertions.assertThat(response.getString("statusMessage")).as(orderDTO.getORDER_ID() + " is not closed").isEqualTo("SUCCESS");

        AutoRefundPeon autoRefundPeon = new AutoRefundPeon(orderDTO.getORDER_ID());
        autoRefundPeon.validatePWPTxn(orderDTO,txnAmount);

    }

    @Parameters({"isNativePlus"})
    @Feature("PGP-30047")
    @Owner(Constants.Owner.ROHIT)
    @Test(description = "verify refund should be initiated automatically for successful cc/dc txn with autoRefund=true and txn amount below autoRefund.max.acceptable.amount in project.theia.properties ")
    public void PGP_verifyAutoRefundWithAmountLessThanLimitAmount(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_Refund_auto;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setAutoRefund(true)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PG2_Refund_auto, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep -A 20 \"" + "FLUXNET_BANKCARD_REFUND_RESULT" + "\" /paytm/logs/instaproxy.log | " + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String instaproxyLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(instaproxyLogs).contains("\"resultCode\":\"ACCEPTED_SUCCESS\"");


    }
    @Parameters({"isNativePlus"})
    @Feature("PGP-30047")
    @Owner(Constants.Owner.ROHIT)
    @Test(description = "verify invalid parameter response when initiate txn with autoRefund=true and txn amount above autoRefund.max.acceptable.amount in project.theia.properties.")
    public void PGP_verifyResponseWithAmountGraterThanLimitAmount(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_Refund_auto;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("60")
                .setAutoRefund(true)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath InitTxnJson = initTxn.execute().jsonPath();
        Assertions.assertThat(InitTxnJson.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");



    }
    @Parameters({"isNativePlus"})
    @Feature("PGP-30047")
    @Owner(Constants.Owner.ROHIT)
    @Test(description = "verify refund should be initiated automatically for successful cc/dc txn with autoRefund=true and txn amount equal autoRefund.max.acceptable.amount in project.theia.properties")
    public void PGP_verifyAutoRefundWithAmountEqualToLimitAmount(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_Refund_auto;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("50")
                .setAutoRefund(true)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PG2_Refund_auto, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateTxnDate(new Date())
                .AssertAll();
        String grepcmd = "grep -A 20 \"" + "FLUXNET_BANKCARD_REFUND_RESULT" + "\" /paytm/logs/instaproxy.log | " + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String instaproxyLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(instaproxyLogs).contains("\"resultCode\":\"ACCEPTED_SUCCESS\"");
    }


}
