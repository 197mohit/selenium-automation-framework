package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.wallet.WalletPaymentConfirmation;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.appconstants.Constants.*;
import static com.paytm.apphelpers.RefundStatusV1Helper.PAY_METHODS;
import static com.paytm.base.test.Group.Theme;
import static org.awaitility.Awaitility.with;

@Owner("Gagandeep")
public class RefundStatusV1 extends PGPBaseTest {

    //TODO:Lot of functions have been repeated in this class need to remove duplicate code

    private final CheckoutPage checkoutPage = new CheckoutPage();

    private String getAsyncRefundResultCode(Response response) {
        try {
            return response.jsonPath().get("body.resultInfo.resultCode");
        } catch (NullPointerException ex) {
            throw new RuntimeException(" body OR resultInfo OR resultCode is not present in response: '" + response.jsonPath() + "'");
        }
    }


    public void addMPABalanceToMerchant(String mid){

        String txnAmount = "100.00";//To increase Merchant's MPA balance so that merchant have balance to give back refund to user
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.getByMid(mid))
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.getByMid(mid), initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response for success PGOnly CC transaction.", groups = "smoke")
    public void successfulPGOnlyCCRefund_asyncStatus(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.PGOnly_PG2_Refund.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);

            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.PGOnly_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateRefundDetailInfoList(PAY_METHODS.CREDIT_CARD, orderDTO.getTXN_AMOUNT(), "HDFC")
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of PGOnly DC transaction.")
    public void successfulPGOnlyDCRefund_asyncStatus(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT("10.50").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.PGOnly_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateRefundDetailInfoList(PAY_METHODS.DEBIT_CARD, orderDTO.getTXN_AMOUNT(), "")
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of walletOnly transaction")
    public void successfulWalletOnlyRefund_asyncStatus(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.WalletOnly_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.WalletOnly_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateRefundDetailInfoList(PAY_METHODS.BALANCE, orderDTO.getTXN_AMOUNT(), null)
                    .asserAll());
        }
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify /v1/refundStatus success response of PGOnly UPI transaction", enabled = false)//TODO will enable once upi refund mock is made
    public void successfulPGOnlyUPIRefund_asyncStatus(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.PGOnly, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of Hybrid Transaction.")
    public void successfulHybridRefund_asyncStatus(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.REFUND_HYBRID.getId());
            User user = userManager.getForRead(Label.BASIC, Label.NOPOSTPAID);
            OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.REFUND_HYBRID, theme, user)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            PaymentDTO paymentDTO = new PaymentDTO();
            cashierPage.payBy(PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
        }
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.REFUND_HYBRID, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFCBANK.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();


        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);

            String walletRefundAmount = Double.toString(amountToBeRetainedInWallet);
            String pgRefundAmount = Double.toString(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.REFUND_HYBRID, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(Double.valueOf(orderDTO.getTXN_AMOUNT()))
                    .validateTotalRefundAmount(Double.valueOf(orderDTO.getTXN_AMOUNT()))
                    .validateRefundDetailInfoList(PAY_METHODS.BALANCE, Double.valueOf(walletRefundAmount), null)
                    .validateRefundDetailInfoList(PAY_METHODS.CREDIT_CARD, Double.valueOf(pgRefundAmount), "HDFC")
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of Add N Pay transaction")
    public void successfulAddNPayRefund_asyncStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.AddnPay_PG2_Refund.getId());
            User user = userManager.getForRead(Label.ADVANCEDEPOSIT);
            OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_Refund, theme, user)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            PaymentDTO paymentDTO = new PaymentDTO();
            cashierPage.payBy(PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.VISA_CREDIT_CARD_NUMBER));
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
        }
        User user = userManager.getForWrite(Label.FOODWALLET);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay_PG2_Refund, theme, user).setTXN_AMOUNT("3.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.AddnPay_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateRefundDetailInfoList(PAY_METHODS.BALANCE, orderDTO.getTXN_AMOUNT(), null)
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of partial refund.")
    public void successfulPartialRefund_asyncStatus(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String refundAmount = "1.00";
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), refundAmount,
                    "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.PGOnly_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(refundAmount)
                    .validateTotalRefundAmount(refundAmount)
                    .validateRefundDetailInfoList(PAY_METHODS.CREDIT_CARD, refundAmount, "HDFC")
                    .asserAll());
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response for which a partial refund was already succeeded but the total refund amount doesn't exceed the actual txn amount")
    public void successfulMultiplePartialRefund_asyncStatus(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.50",
                    "REFUND", "", null);
            PGPHelpers.getRefundStatusV1(MerchantType.PGOnly_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateRefundAmount("1.50")
                    .validateTotalRefundAmount("1.50")
                    .asserAll();

            /* Raising refund for pending amount and validating same */
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID() + "1234", txnStatus.getResponse().getTXNID(), "1.50",
                    "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.PGOnly_PG2_Refund, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateRefundAmount("1.50")
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateRefundDetailInfoList(PAY_METHODS.CREDIT_CARD, "1.50", "")
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of base amount in post convenience transaction when CF remain same.")
    public void successfulRefundOfBaseAmountInPostConvenienceTxn_asyncStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = MerchantType.FLAT_PCF_Pg2_Refund.getId();
        String key = MerchantType.FLAT_PCF_Pg2_Refund.getKey();

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
            addMPABalanceToMerchant(mid);
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, key, theme).setTXN_AMOUNT("3.00").build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateSuccessResponse()
                    .AssertAll();

            Response response = PGPHelpers.initiateAsyncRefund(order.getMID(), order.getMerchantKey(),
                    order.getORDER_ID(), order.getORDER_ID(), txnStatus.getResponse().getTXNID(), order.getTXN_AMOUNT(),
                    "C", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(order.getMID(), order.getORDER_ID(), order.getORDER_ID(), order.getMerchantKey(), true)
                    .validateSuccessRefund()
                    .validateTotalRefundAmount(order.getTXN_AMOUNT())
                    .validateRefundAmount(order.getTXN_AMOUNT())
                    .asserAll());
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response when wallet amount is already breaching wallet limits", groups = Group.Status.TO_BE_FIXED)
    public void successfulRefundForWalletLimitBreach_asyncStatus(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.WalletOnly.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.WalletOnly, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        try {
            WalletHelpers.breachAddMoneyLimit(user);

            Test:
            {
                PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                        orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                        "REFUND", "", null);
                with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                        .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.WalletOnly, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                        .validateSuccessRefund()
                        .asserAll());
            }
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response of base amount with fee in post convenience transaction when CF remain same.")
    public void successfulRefundOfBaseAmountWithFeeInPostConvenienceTxn_asyncStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = MerchantType.FLAT_PCF_Pg2_Refund.getId();
        String key = MerchantType.FLAT_PCF_Pg2_Refund.getKey();
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
            addMPABalanceToMerchant(mid);
        }
        OrderDTO order = new OrderFactory.PGOnly(mid, key, theme).setTXN_AMOUNT("3.00").build();
        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateAsyncRefund(order.getMID(), order.getMerchantKey(),
                    order.getORDER_ID(), order.getORDER_ID(), txnStatus.getResponse().getTXNID(), "3.00",
                    "R", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(mid, order.getORDER_ID(), order.getORDER_ID(), key, true)
                    .validateSuccessRefund()
                    .validateRefundAmount(Double.valueOf(order.getTXN_AMOUNT()))
                    .validateTotalRefundAmount(Double.valueOf(order.getTXN_AMOUNT()))
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify /v1/refundStatus success response refund of base amount in post convenience transaction when CF remain same.")
    public void successfulPartialRefundOfBaseAmountInPostConvenienceTxn_asyncStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = MerchantType.FLAT_PCF_Pg2_Refund.getId();
        String key = MerchantType.FLAT_PCF_Pg2_Refund.getKey();
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
            addMPABalanceToMerchant(mid);
        }
        OrderDTO order = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String partialRefundAmount = String.valueOf(Double.valueOf(order.getTXN_AMOUNT()) - 1.00);
        Test:
        {
            PGPHelpers.initiateAsyncRefund(order.getMID(), order.getMerchantKey(),
                    order.getORDER_ID(), order.getORDER_ID(), txnStatus.getResponse().getTXNID(), partialRefundAmount,
                    "C", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(mid, order.getORDER_ID(), order.getORDER_ID(), key, true)
                    .validateSuccessRefund()
                    .validateRefundAmount(Double.valueOf(partialRefundAmount))
                    .validateTotalRefundAmount(Double.valueOf(partialRefundAmount))
                    .asserAll());
        }
    }

    @Parameters({"theme"})
    @Test(groups = Theme.MERCHANT4,
            description = "To verify /v1/refundStatus success response for Subs First Request via CC")
    public void verifySuccessfulRefundOnSubsViaCC_asyncStatus(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.Subscription_PGOnly.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(Constants.MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();

        if (PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID()).isEmpty())
            throw new SkipException("Acquirement Id not found, subscription is not successfull");
        if (PGPHelpers.getSavedCardId(subsId).isEmpty())
            throw new SkipException("Saved Card Id not found");

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1(MerchantType.Subscription_PGOnly, orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .asserAll());
        }

    }

    //Async Refund

    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in ASYNC REFUND API for CC payMode")
    public void refundCCWithJWTAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");



        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);

            with().pollInSameThread().await().pollInterval(Duration.FIVE_SECONDS)
                    .atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()->Assertions.assertThat(PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()));
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is false in ASYNC REFUND API for CC payMode")
    public void refundUPICCWithJWTAsyncRefundDisableFalse(@Optional("enhancedweb") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", false, "Initiate Refund disableFalse",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in ASYNC REFUND API for DC payMode")
    public void refundDCWithJWTAsyncRefund(@Optional("enhancedweb") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is false in ASYNC REFUND API for DC payMode")
    public void refundDCWithJWTAsyncRefundDisableFalse(@Optional("enhancedweb") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", false, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }
    }

    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in ASYNC REFUND API for UPI payMode")
    public void refundUPIWithJWTAsyncRefund(@Optional("enhancedweb") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .validateMaskedVPA()
                    .asserAll());
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in ASYNC REFUND API for Wallet payMode")
    public void refundPPIWithJWTAsyncRefund(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is false in ASYNC REFUND API for Wallet payMode")
    public void refundPPIWithJWTAsyncRefundDisabledFalse(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", false, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in ASYNC REFUND API for PPBL payMode")
    public void refundPPBLWithJWTAsyncRefundDisabledTrue(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);

            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()->PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                            .validateSuccessRefund()
                            .validateMid(orderDTO.getMID())
                            .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                            .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                            .validateAgentInfo()
                            .asserAll());
        }
    }


    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is false in ASYNC REFUND API for PPBL payMode")
    public void refundPPBLWithJWTAsyncRefundDisabledFalse(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Async Refund with JWT Token
        Test:
        {
            PGPHelpers.initiateAsyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", false, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }
    }




    //Sync Refund

    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in SYNC REFUND API for CC payMode")
    public void refundCCWithJWTSyncRefund(@Optional("enhancedweb") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.VISA_CREDIT_CARD_NUMBER));
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Sync Refund with JWT Token

        {
            PGPHelpers.initiatesyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }



    }

    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in SYNC REFUND API for DC payMode")
    public void refundDCWithJWTSyncRefund(@Optional("enhancedweb") String theme) {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Sync Refund with JWT Token

        {
            PGPHelpers.initiatesyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }



    }

    @Owner("Tarun")
    @Issue("PGP-26673")
    @Parameters({"theme"})
    @Test(description = "Verify if MPA balance is present and disableMerchantRetry is true in SYNC REFUND API for PPI payMode")
    public void refundPPIWithJWTSyncRefund(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType refundMerchant = MerchantType.PGOnly_PG2_Refund;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        //Creating JWT Token

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("iss","client123");
        tokenMap.put("mid", refundMerchant.getId());
        String refId = "Ref" + orderDTO.getORDER_ID();
        tokenMap.put("refId",refId);

        String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,"abcd");

        //Sync Refund with JWT Token

        {
            PGPHelpers.initiatesyncRefundJWT(orderDTO.getMID(),
                    orderDTO.getORDER_ID(), refId, txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", true, "Initiate Refund",jwtToken);
            with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS).atMost(Duration.ONE_MINUTE)
                    .untilAsserted(()-> PGPHelpers.getRefundStatusV1JWT(refundMerchant,orderDTO.getORDER_ID(),refId,"JWT",jwtToken,true)
                    .validateSuccessRefund()
                    .validateMid(orderDTO.getMID())
                    .validateRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateTotalRefundAmount(orderDTO.getTXN_AMOUNT())
                    .validateAgentInfo()
                    .asserAll());
        }



    }


}
