package scripts.PureHybrid;

import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.HybridPayModeDetail;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.awaitility.Awaitility.with;

public class PureHybrid extends PGPBaseTest {

    CheckoutPage checkoutPage = new CheckoutPage();


    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & CC) Enhanced flow")
    public void verifyPureHybrid_01(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double
                .parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateChildTxnPresent(TxnStatus.ChildTxnType.CC)
                .AssertAll();

    }


    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & DC) Enhanced flow")
    public void verifyPureHybrid_02(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double
                .parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateChildTxnPresent(TxnStatus.ChildTxnType.DC)
                .AssertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & NB) Enhanced flow")
    public void verifyPureHybrid_03(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double
                .parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateChildTxnPresent(TxnStatus.ChildTxnType.NB)
                .AssertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & UPI) Enhanced flow")
    public void verifyPureHybrid_04(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double
                .parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .AssertAll();

    }

    private final CheckoutJsCheckoutPage checkoutJsCheckoutPage = new CheckoutJsCheckoutPage();

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & NB) Checkout JS flow NB")
    public void verifyPureHybrid_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants
                .MerchantType.HybridCheckoutJS)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }


    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid(PPI & CC)Checkout JS flow")
    public void verifyPureHybrid_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants
                .MerchantType.HybridCheckoutJS)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }


    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid(PPI & DC)Checkout JS flow")
    public void verifyPureHybrid_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants
                .MerchantType.PURE_HYBRID)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid(PPI & UPI)Checkout JS flow")
    public void verifyPureHybrid_08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants
                .MerchantType.HybridCheckoutJS)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that If txn amount is > 5000, Wallet should be disabled Checkout JS flow ")
    public void verifyPureHybrid_011(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 5500.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.HybridCheckoutJS)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,5499.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.isWalletDisabled());
        Assert.assertFalse(cashierPage.isPPIChecked());
        Assert.assertEquals(cashierPage.HybridInsufficientWalletAmtMsg()
                .getText(), "You have insufficient funds in your Paytm Wallet Account. Please use other payment option to complete this transaction.");
    }



    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify that If txn amount is > 5000, Wallet should be disabled Enhanced flow")
    public void verifyPureHybrid_12(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 5500.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double
                .parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertEquals(cashierPage.HybridInsufficientWalletAmtMsg()
                .getText(), "You have insufficient funds in your Paytm Bank Account. Please use other payment option to complete this transaction.");
    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify order status or Get payment status if txn done with Enhanced flow")
    public void verifyPureHybrid_13(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO
                .getTXN_AMOUNT()) -1);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), Constants
                        .MerchantType.PURE_HYBRID.getKey(), orderDTO.getMID()).build();
        
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("HYBRID");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[0].paymentMode")).isEqualToIgnoringCase("NB");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[1].paymentMode")).isEqualToIgnoringCase("PPI");

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner("ANUSHKA_GOLDI")
    @Test(description = "Verify order status or Get payment status if txn done with Checkout JS flow NB")
    public void verifyPureHybrid_14(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants
                .MerchantType.HybridCheckoutJS)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(),  initTxnDTO.getBody().getMid(), Constants.MerchantType.HybridCheckoutJS.getKey())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("HYBRID");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[0].paymentMode")).isEqualToIgnoringCase("NB");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[1].paymentMode")).isEqualToIgnoringCase("PPI");

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & PPBL) Enhanced flow")
    public void verifyPureHybrid_15(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .AssertAll();

    }


    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & Saved VPA) Enhanced flow")
    public void verifyPureHybrid_16(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .AssertAll();

    }


    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify that WALLET amount is 1.00 txn amt is 2 with and pure Hybrid (PPI & Postpaid) Enhanced flow")
    public void verifyPureHybrid_17(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        PostpaidHelpers.updateBalance("10");


        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .AssertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify order status or Get payment status if txn done with Checkout JS flow Postpaid")
    public void verifyPureHybrid_18(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PURE_HYBRID)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        PostpaidHelpers.updateBalance("10");
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(),  initTxnDTO.getBody().getMid(), Constants.MerchantType.HybridCheckoutJS.getKey())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("HYBRID");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[0].paymentMode")).isEqualToIgnoringCase("Paytm Postpaid");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[1].paymentMode")).isEqualToIgnoringCase("PPI");

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify order status or Get payment status if txn done with Checkout JS flow Saved VPA")
    public void verifyPureHybrid_19(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PURE_HYBRID)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(),  initTxnDTO.getBody().getMid(), Constants.MerchantType.HybridCheckoutJS.getKey())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("HYBRID");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[0].paymentMode")).isEqualToIgnoringCase("UPI");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[1].paymentMode")).isEqualToIgnoringCase("PPI");

    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify successful full refund of a hybrid + cc transaction")
    public void verifyPureHybrid_20(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateChildTxnPresent(TxnStatus.ChildTxnType.CC)
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), Constants.MerchantType.PURE_HYBRID.getKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(Constants.MerchantType.PURE_HYBRID, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                            .validateSuccessRefund()
                            .validateMid(orderDTO.getMID())
                            .validateRefundAmount("2.00")
                            .validateTotalRefundAmount("2.00")
                            .validateRefundDetailInfoList(RefundStatusV1Helper.PAY_METHODS.BALANCE, "1.00", null)
                            .validateRefundDetailInfoList(RefundStatusV1Helper.PAY_METHODS.CREDIT_CARD, "1.00", null)
                            .asserAll());
        }
    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify successful partial refund of a hybrid + cc transaction")
    public void verifyPureHybrid_21(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Double txnAmount = 5.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantId, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -3);

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.insufficientBalanceIconMsg().isDisplayed()){
//            cashierPage.checkBoxPPI().check(); //wallet need to be checked
//        }

        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateTxnDate(new Date())
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateChildTxnPresent(TxnStatus.ChildTxnType.CC)
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), Constants.MerchantType.PURE_HYBRID.getKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), "3", txnStatus.getResponse().getTXNID(), "");
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(Constants.MerchantType.PURE_HYBRID, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                            .validateSuccessRefund()
                            .validateMid(orderDTO.getMID())
                            .validateRefundAmount("3.00")
                            .validateTotalRefundAmount("3.00")
                            .validateRefundDetailInfoList(RefundStatusV1Helper.PAY_METHODS.BALANCE, "2.00", null)
                            .validateRefundDetailInfoList(RefundStatusV1Helper.PAY_METHODS.CREDIT_CARD, "1.00", null)
                            .asserAll());
        }
    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify successful native transaction of hybrid + cc transaction")
    public void verifyPureHybrid_22() throws Exception {

        double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        String merchantId = Constants.MerchantType.PURE_HYBRID.getId();
        WalletHelpers.modifyBalance(user,txnAmount -1.0);
        double walletBalance = WalletHelpers.getWalletBalance(user);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PURE_HYBRID)
                .setSsoToken(user.ssoToken())
                .setTxnValue(Double.toString(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assert.assertEquals(fetchPaymentOptionsJson.getString("body.supportedPaymentFlows"),"[HYBRID]");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.hybridMode")).containsOnlyOnce("PRIMARY");

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("BALANCE");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(Double.toString(walletBalance));
        hybridPayModeDetail2.setPaymentMode("NET_BANKING");
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0,hybridPayModeDetail1);
        hybridPayModeDetailList.add(1,hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantId,initTxnDTO.orderFromBody(),txnToken)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.orderFromBody(), merchantId, Constants.MerchantType.PURE_HYBRID.getKey(), merchantId)
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.orderFromBody());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(merchantId);

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("HYBRID");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[0].paymentMode")).isEqualToIgnoringCase("NB");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[0].txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[1].txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(getPaymentStatus.getString("body.childTransaction[1].paymentMode")).isEqualToIgnoringCase("PPI");
    }

    @Parameters({"theme"})
    @Feature("PGP-48994")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify order status or Get payment status if txn done with Checkout JS flow PPBL")
    public void verifyPureHybrid_23(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PURE_HYBRID)
                .setTxnValue(txnAmount.toString())
                .build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
//        if(cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()){
//            cashierPage.uncheckedPPIForCheckoutJS().click();
//        }
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateChildTxnsPresent()
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner("Meenakshi")
    @Feature("PGP-54571")
    @Parameters({"theme"})
    @Test(description = "To verify the successful wallet transaction when wallet status is in credit freeze")
    public void hybridwalletsuccess(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.INACTIVEWALLETZEROBAL);
        Constants.MerchantType merchantType = Constants.MerchantType.DEALS_PURE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType).setTxnValue("1100").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.supportedPaymentFlows")).contains("HYBRID");

        List<HybridPayModeDetail> hybridPayModeDetailList = new ArrayList<>();
        HybridPayModeDetail hybridPayModeDetail1 = new HybridPayModeDetail();
        HybridPayModeDetail hybridPayModeDetail2 = new HybridPayModeDetail();
        hybridPayModeDetail1.setPaymentMode("BALANCE");
        hybridPayModeDetail1.setPaymodeSequence(1);
        hybridPayModeDetail1.setHybridLevel("PRIMARY");
        hybridPayModeDetail1.setHybridAmount(Double.toString(1000));
        hybridPayModeDetail2.setPaymentMode("CREDIT_CARD");;
        hybridPayModeDetail2.setPaymodeSequence(2);
        hybridPayModeDetail2.setHybridLevel("SECONDARY");
        hybridPayModeDetailList.add(0,hybridPayModeDetail1);
        hybridPayModeDetailList.add(1,hybridPayModeDetail2);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(hybridPayModeDetailList, merchantType.getId(),initTxnDTO.orderFromBody(),txnToken)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4761360075860477|333|122025")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount("1100")
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(merchantType.getId())
                .validatePaymentMode("HYBRID")
                .validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateChildTxnPresent(TxnStatus.ChildTxnType.CC)
                .AssertAll();
    }



}
