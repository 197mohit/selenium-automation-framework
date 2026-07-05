package scripts.Native;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
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
import scripts.PostCovEnhance;

import java.util.Date;

/**
 * Created by anjukumari on 08/05/19
 */

@Owner("Tarun")
public class PCFNative extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final PcfHelpher pcfHelpher = new PcfHelpher();


    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Native CC transaction, also validate binDetail and fetchPayOption API when SSo token is not passed in request.")
    public void TC_PT_001(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NETBANK_PCF_PG2_RTDD).build();
        String txnToken = pcfHelpher.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        pcfHelpher.Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        Response binResponse = pcfHelpher.validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getCreditCardNumber()));
        pcfHelpher.validate_BinDetail_pcf(binResponse.jsonPath(), initTxnDTO.txnAmountFromBody(), Double.parseDouble(PostCovEnhance.pcf_CC), "3", "VISA", "13");
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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_CC));
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Native DC transaction also validate binDetail and fetchPayOption API when SSo token is passed in request.")
    public void TC_PT_002(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NETBANK_PCF_PG2_RTDD).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        pcfHelpher.Validate_FetchPayInstrument(txnToken, initTxnDTO, "DEBIT_CARD", "false");
        Response binResponse = pcfHelpher.validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(new PaymentDTO().getDebitCardNumber()));
        pcfHelpher.validate_BinDetail_pcf(binResponse.jsonPath(), initTxnDTO.txnAmountFromBody(), Double.parseDouble(PostCovEnhance.pcf_DC_upTo10), "3", "VISA", "13");
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
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_DC_upTo10));
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify NB transaction when SSo token is passed in request.")
    public void TC_PT_003(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NETBANK_PCF_PG2_RTDD).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        pcfHelpher.Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
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
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_NETBANKING_ICICI));
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify PPI transaction success when SSO Token is passed in initTransaction request.")
    public void TC_PCF_PPI(@Optional("false") Boolean isNativePlus) throws Exception {
        Double txnAmount = 2.15;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.valueOf(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount + convenienceFeeCalculatorOld(txnAmount, 0.0, Double.parseDouble(PostCovEnhance.pcf_wallet), "PPI"))));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NETBANK_PCF_PG2_RTDD)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE).build();
        JsonPath path = pcfHelpher.Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getDouble("body.merchantPayOption.paymentModes[0].payChannelOptions[0].balanceInfo.accountBalance.value")).isEqualTo(WalletHelpers.getWalletBalance(user));
        pcfHelpher.validate_Fee_inPcfDetail(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, PostCovEnhance.pcf_wallet, "BALANCE", "Paytm Balance", null);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_wallet));

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay success transaction also validate payment options as BALANCE in fetchpayoption and paymentflow as ADDANDPAY.", enabled = true)
    public void TC_PCF_ADDNPAY(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NETBANK_PCF_PG2_RTDD;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        pcfHelpher.validate_Fee_inPcfDetail(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, PostCovEnhance.pcf_wallet, "BALANCE", "Paytm Balance", null);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        JsonPath path = new PcfHelpher().Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_wallet));

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify add n pay success transaction with zero wallet balance", enabled = true)
    public void TC_PCF_AddNPayWithZeroWalletbalance(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NETBANK_PCF_PG2_RTDD;
        Double txnAmount = 2.0;
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();
        JsonPath path = new PcfHelpher().Validate_FetchPayInstrument(txnToken, initTxnDTO, "BALANCE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        pcfHelpher.validate_Fee_inPcfDetail(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, PostCovEnhance.pcf_wallet, "BALANCE", "Paytm Balance", null);
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_wallet));
    }

    @Issue("PGP-14970")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify UPI Transaction.")
    public void TC_PCF_UPI(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NETBANK_PCF_PG2_RTDD;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();
        JsonPath path = new PcfHelpher().Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");
        //validate Pcf fee for UPI
        pcfHelpher.validate_Fee_inPcfDetail(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, PostCovEnhance.pcf_UPI_upTo10, "UPI", "BHIM UPI", null);
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
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_UPI_upTo10));

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn using saved cards for CVV of 3 digits.", enabled = true)
    public void TC_PCF_SaveCard(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NETBANK_PCF_PG2_RTDD;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();

        //validate Pcf fee for CC
        pcfHelpher.validate_Fee_inPcfDetail(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, PostCovEnhance.pcf_CC, "CREDIT_CARD", "Credit Card", "HDFC");

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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_CC));
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn using saved cards for CVV of 4 digits.")
    public void TC_PT_SaveCardAmex(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AMEX_PCF;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), PaymentDTO.AMEX_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId = initTxnDTO.orderFromBody();
        String txnToken = response.jsonPath().get("body.txnToken").toString();

        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("1234");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();

        //validate Pcf fee for CC
        pcfHelpher.validate_Fee_inPcfDetail(Constants.MerchantType.AMEX_PCF, PostCovEnhance.pcf_Amex, "CREDIT_CARD", "Credit Card", "AMEX");

        Response binResponse = pcfHelpher.validate_BinDetail(txnToken, initTxnDTO, orderDTO, CommonHelpers.getCardFirstSixDigit(PaymentDTO.AMEX_CARD_NUMBER));
        pcfHelpher.validate_BinDetail_pcf(binResponse.jsonPath(), initTxnDTO.txnAmountFromBody(), Double.parseDouble(PostCovEnhance.pcf_Amex), "4", "AMEX", "15");

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
                .validateGatewayName(Constants.Gateway.AMEX.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.AMEX.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        pcfHelpher.validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_Amex));
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Add money for wallet only PCF merchant")
    public void TC_AddMOneyWalletOnlyPCFMerchant(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADD_MONEY_PCF_PG2_RTDD).setIsNativeAddMoney("true").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.ADD_MONEY_PCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(initTxnDTO.txnAmountFromBody()));
    }



    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Add money with DC when DC aquiring is present on PCF merchant ")
    public void TC_AddMOneyWithDCAcquiringPCFMerchant(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NETBANK_PCF_PG2_RTDD).setIsNativeAddMoney("true").setTxnValue("2").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NETBANK_PCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(initTxnDTO.txnAmountFromBody()));
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Add money with DC when DC aquiring is present on Non PCF merchant ")
    public void TC_AddMOneyWithDCAcquiringNonPCFMerchant(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddMoney).setIsNativeAddMoney("true").setTxnValue("2").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddMoney, initTxnDTO.orderFromBody(), txnToken,PayMethodType.DEBIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(initTxnDTO.txnAmountFromBody()));
    }


}
