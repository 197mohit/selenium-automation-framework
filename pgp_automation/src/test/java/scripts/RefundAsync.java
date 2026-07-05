package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.RefundApi;
import com.paytm.api.TxnStatus;
import com.paytm.api.RefundReversal;
import com.paytm.api.wallet.WalletPaymentConfirmation;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.MerchantManager;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.PTYBLIIntentCallback;
import com.paytm.api.Deals.GetPaymentStatus;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.DefaultCommission;
import com.paytm.utils.merchant.merchant.ExistingMerchantContract;
import com.paytm.utils.merchant.merchant.Merchant;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

import com.paytm.api.RefundStatusApi;
import com.paytm.api.MasterRefundStatusApi;
import com.paytm.api.refund.SyncRefund;
import static io.restassured.RestAssured.given;

/**
 * Created by ankuragarwal on 24/12/18
 */
@Owner("Ankur and Tarun")
@Epic(Constants.Sprint.SPRINT31_1)
@Feature("PGP-19800")

public class RefundAsync extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String postConvFlag = "";
    private static final String RESULT_CODE_601 = "601";
    private static final String ALLOWED_TXN_AMT = "4.00";
    private static final String TXN_SUCCESS = "TXN_SUCCESS";

    private String getAsyncRefundResultCode(Response response) {
        try {
            return response.jsonPath().get("body.resultInfo.resultCode");
        } catch (NullPointerException ex) {
            throw new RuntimeException(" body OR resultInfo OR resultCode is not present in response: '" + response.jsonPath() + "'");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of PGOnly CC transaction with transaction amount as whole number.", groups = "smoke")
    public void successfulPGOnlyCCRefund_async(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validateCheckSum(Constants.MerchantType.PGOnly_PG2_Refund.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of PGOnly DC transaction with transaction amount in decimal number.")
    public void successfulPGOnlyDCRefund_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT("10.50").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of walletOnly transaction")
    public void successfulWalletOnlyRefund_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.WalletOnly_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.WalletOnly_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateGATEWAY(txnStatus.getResponse().getGATEWAYNAME(), 0)
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }


    }

 //   @Parameters({"theme"})
 //   @Test(description = "Verify successful refund of PGOnly UPI transaction", enabled = false) // TODO need to create UPI refund mock
    public void successfulPGOnlyUPIRefund_async(@Optional("merchant4") String theme) {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(), "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateGATEWAY(txnStatus.getResponse().getGATEWAYNAME(), 0)
                    .validatePAYMENTMODE(txnStatus.getResponse().getPAYMENTMODE(), 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of Hybrid Transaction.")
    public void successfulHybridRefund_async(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.REFUND_HYBRID.getId());
            User user = userManager.getForRead(Label.BASIC, Label.NOPOSTPAID);
            OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.REFUND_HYBRID, theme, user)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
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
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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

            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true);

            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            map = refundStatusHelper.getRefundBy("PAYMENTMODE", "CC");
            refundStatusHelper.validate(map, "GATEWAY", "HDFC");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.assertAll();
        }
    }

    @Issue("PGP-20437")
    @Parameters({"theme"})
    @Test(description = "Verify successful refund of Add N Pay transaction")
    public void successfulAddNPayRefund_async(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {

            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_PG2_Refund.getId());
            User user = userManager.getForRead(Label.BASIC);
            OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay_PG2_Refund, theme, user)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();

        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay_PG2_Refund, theme, user)
                .setTXN_AMOUNT("3.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateStatus(TXN_SUCCESS, 1)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund.")
    public void successfulPartialRefund_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT("1.00", 0)
                    .validateTOTALREFUNDAMT("1.00", 0)
                    .assertAll();
        }

    }


    @Parameters({"theme"})
    @Test(description = "Verify refund failure of the transaction whose refund has already been succeeded.")
    public void verifyRefundFailureOfAlreadySuccessfulRefund_async(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();

            /* Again raising refund and validating response*/
            Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID() + "1234", txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("619");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULTMSG mismatch").isEqualToIgnoringCase("Invalid refund amount.");
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify refund failure of failed PGonly Transaction.")
    public void verifyRefundFailureOfFailedPGOnlyTxn_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateFailureResponse(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode(),
                        Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();

        Test:
        {
            Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("679");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify the successful refund of txn for which a partial refund was already succeeded but the total refund amount doesn't exceed the actual txn amount")
    public void successfulMultiplePartialRefund_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.50",
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT("1.50", 0)
                    .validateTOTALREFUNDAMT("1.50", 0)
                    .assertAll();

            /* Raising refund for pending amount and validating same */
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID() + "1234", txnStatus.getResponse().getTXNID(), "1.50",
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID()+ "1234", true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT("1.50", 0)
                    .validateTOTALREFUNDAMT(orderDTO.getTXN_AMOUNT(), 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify refund failure of txn for which a partial refund was already succeeded but the total refund amount exceeds the actual txn amount.")
    public void failurePartilRefundDoneTwiceThatExceedsTxnAmount_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.50",
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT("1.50", 0)
                    .validateTOTALREFUNDAMT("1.50", 0)
                    .assertAll();

            /* Raising refund for amount more that total txn amount and validating failure*/
            Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID() + "1234", txnStatus.getResponse().getTXNID(), "2.50",
                    "REFUND", "", null);
            Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("619");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("TXN_FAILURE");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("ResultMsg mismatch").isEqualToIgnoringCase("Invalid refund amount.");
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of base amount in post convenience transaction when CF remain same.")
    public void successfulRefundOfBaseAmountInPostConvenienceTxn_async(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
        }
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, key, theme).build();
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
            PGPHelpers.getRefundStatus(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT(order.getTXN_AMOUNT(), 0)
                    .validateTOTALREFUNDAMT(order.getTXN_AMOUNT(), 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Parameter validation: Verify refund failure with incorrect mid, orderID and TxnId")
    public void refundFailureWithIncorrectMidOrderIdTxnId_async(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        InvalidOrderIdValidation:
        {
            Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID() + "ab", orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("627");
        }
        InvalidTxnIdValidation:
        {
            Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID() + "abc", orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("617");
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify wallet refund when wallet amount is already breaching wallet limits", groups = Group.Status.TO_BE_FIXED)
    public void successfulRefundForWalletLimitBreach_async(@Optional("merchant4") String theme) throws Exception {
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
                PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                        .validateSuccessRefund()
                        .assertAll();
            }
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of base amount with fee in post convenience transaction when CF remain same.")
    public void successfulRefundOfBaseAmountWithFeeInPostConvenienceTxn_async(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
        }
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }

        OrderDTO order = new OrderFactory.PGOnly(mid, key, theme).build();
        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        double commissionAmount = convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
        String totalRefundAmount = String.valueOf(Math.round((commissionAmount + Double.valueOf(order.getTXN_AMOUNT())) * 100.0) / 100.0);

        Test:
        {
            PGPHelpers.initiateAsyncRefund(order.getMID(), order.getMerchantKey(),
                    order.getORDER_ID(), order.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                    "R", "", null);
            PGPHelpers.getRefundStatus(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT(order.getTXN_AMOUNT(), 0)
                    .validateTOTALREFUNDAMT(totalRefundAmount, 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund of base amount in post convenience transaction when CF remain same.")
    public void successfulPartialRefundOfBaseAmountInPostConvenienceTxn_async(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
        }
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }

        OrderDTO order = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
            PGPHelpers.getRefundStatus(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .validateREFUNDAMOUNT(Double.valueOf(partialRefundAmount), 0)
                    .validateTOTALREFUNDAMT(Double.valueOf(partialRefundAmount), 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful refund for Subs First Request via CC")
    public void verifySuccessfulRefundOnSubsViaCC_async(@Optional("merchant4") String theme) throws Exception {
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
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }

    }
    @Parameters({"theme"})
    @Test(description = "To verify successful refund for Subs Renewal Txn")
    public void verifySuccessfulRefundOfSubsRenewalTxn_async(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.Subscription_PGOnly.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(Constants.MerchantType.Subscription_PGOnly, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();

        if (PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID()).isEmpty())
            throw new SkipException("Acquirement Id not found, subscription is not successfull");
        if (PGPHelpers.getSavedCardId(subsId).isEmpty())
            throw new SkipException("Saved Card Id not found");

        /*Renew Subs*/
        OrderDTO renewOrderDto = new OrderFactory.SubscriptionRenew(Constants.MerchantType.Subscription_PGOnly, subsId, orderDTO.getTXN_AMOUNT()).build();
        Response response=PGPHelpers.executeProcessTransaction(renewOrderDto);
        JsonPath jsonPath = response.jsonPath();
        String respMsg = jsonPath.get("RESPMSG");
        Reporter.report.info("<br>Renew response message: " + respMsg);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase("Subscription Txn accepted.");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, renewOrderDto.getORDER_ID())).isNotNull().isNotEmpty();
        txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), renewOrderDto.getORDER_ID());
        /*response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                renewOrderDto.getORDER_ID(), renewOrderDto.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                "REFUND", "", null);
        Assertions.assertThat(getAsyncRefundResultCode(response)).isEqualToIgnoringCase("601");*/
        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    renewOrderDto.getORDER_ID(), renewOrderDto.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                    "REFUND", "", null);
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), renewOrderDto.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "To validate partial refund error message for 5 Rs twice when the txn amount is 11 rs")
    public void partialRefundErrorMessageRedundantRefund(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("11.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "5.00",
                    "REFUND", "", null);

            Response refund = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "5.00",
                    "REFUND", "", null);

             Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultCode")).as("Result code incorrect for redundant refund")
                    .isEqualTo("617");

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultMsg")).as("Result message incorrect for redundant refund")
                    .isEqualTo("Refund Already Raised");
        }

    }

    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Asyc Refund NOT Allowed for Instant settlement Transaction for NetBanking for Mutual Fund merchant")
    public void validateAsycRefundNotAllowedForInstantSettlementNB(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_SETTLEMENT;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_SETTLEMENT.getId());
        }


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setAggMid(AggrSettlementMerchant.getId())
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {
            Response refund = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                    "REFUND", "", null);

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultCode")).as("Result code incorrect for redundant refund")
                    .isEqualTo("600");

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultMsg")).as("Result message incorrect for Instant Settlement refund")
                    .isEqualTo("Refund not allowed on bank settled transaction");
        }

    }

    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Asyc Refund NOT Allowed for Instant settlement Transaction for DC for Mutual Fund merchant")
    public void validateAsycRefundNotAllowedForInstantSettlementDC(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_SETTLEMENT;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_SETTLEMENT.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(AggrSettlementMerchant.getId())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {
            Response refund = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                    "REFUND", "", null);

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultCode")).as("Result code incorrect for redundant refund")
                    .isEqualTo("600");

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultMsg")).as("Result message incorrect for Instant Settlement refund")
                    .isEqualTo("Refund not allowed on bank settled transaction");
        }

    }



    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Master Refund NOT Allowed for Instant settlement Transaction for NB for Mutual Fund merchant")
    public void validateMasterRefundNotAllowedForInstantSettlementNB(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_SETTLEMENT;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_SETTLEMENT.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setAggMid(AggrSettlementMerchant.getId())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {
            Response refund = PGPHelpers.executeMasterRefund(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),"1.00",txnStatus.getResponse().getTXNID(),"");

            Assertions.assertThat(refund.jsonPath().getString("RESPCODE")).as("Result code incorrect for redundant refund")
                    .isEqualTo("600");

            Assertions.assertThat(refund.jsonPath().getString("RESPMSG")).as("Result message incorrect for Instant Settlement refund")
                    .isEqualTo("Refund not allowed on bank settled transaction");
        }

    }


    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Master Refund NOT Allowed for Instant settlement Transaction for DC for Mutual Fund merchant")
    public void validateMasterRefundNotAllowedForInstantSettlementDC(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_SETTLEMENT;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_SETTLEMENT.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(AggrSettlementMerchant.getId())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {
            Response refund = PGPHelpers.executeMasterRefund(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),"1.00",txnStatus.getResponse().getTXNID(),"");

            Assertions.assertThat(refund.jsonPath().getString("RESPCODE")).as("Result code incorrect for redundant refund")
                    .isEqualTo("600");

            Assertions.assertThat(refund.jsonPath().getString("RESPMSG")).as("Result message incorrect for Instant Settlement refund")
                    .isEqualTo("Refund not allowed on bank settled transaction");
        }

    }





    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Refund Handler NOT Allowed for Instant settlement Transaction for NB for Mutual Fund merchant")
    public void validateRefundHandlerNotAllowedForInstantSettlementNB(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_SETTLEMENT;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_SETTLEMENT.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setAggMid(AggrSettlementMerchant.getId())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {

            RefundApi refundApi = new RefundApi(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),"1.00",txnStatus.getResponse().getTXNID(),"");

            Response response = refundApi.execute();

            Assertions.assertThat(response.jsonPath().getString("RESPCODE")).as("Result code incorrect for redundant refund")
                    .isEqualTo("600");

            Assertions.assertThat(response.jsonPath().getString("RESPMSG")).as("Result message incorrect for Instant Settlement refund")
                    .isEqualTo("Refund not allowed on bank settled transaction");
        }

    }






    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Refund Handler NOT Allowed for Instant settlement Transaction for DC for Mutual Fund merchant")
    public void validateRefundHandlerNotAllowedForInstantSettlementDC(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_SETTLEMENT;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_SETTLEMENT.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(AggrSettlementMerchant.getId())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {

            RefundApi refundApi = new RefundApi(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),"1.00",txnStatus.getResponse().getTXNID(),"");

            Response response = refundApi.execute();

            Assertions.assertThat(response.jsonPath().getString("RESPCODE")).as("Result code incorrect for redundant refund")
                    .isEqualTo("600");

            Assertions.assertThat(response.jsonPath().getString("RESPMSG")).as("Result message incorrect for Instant Settlement refund")
                    .isEqualTo("Refund not allowed on bank settled transaction");
        }

    }


    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Asyc Refund Allowed for Non Instant settlement Transaction for Mutual Fund merchant")
    public void validateAsycRefundAllowedForNonInstantSettlementTxn(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_WO_SETTLEMENT_PAYMODE;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_WO_SETTLEMENT_PAYMODE.getId());
        }


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setAggMid(AggrSettlementMerchant.getId())
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {
            Response refund = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                    "REFUND", "", null);

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultCode")).as("Result code incorrect for redundant refund")
                    .isEqualTo("601");

            Assertions.assertThat(refund.jsonPath().getString("body.resultInfo.resultMsg")).as("Result message incorrect for Instant Settlement refund")
                    .contains("Refund request was raised for this transaction. But it is pending state");
        }

    }





    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Master Refund Allowed for Non Instant settlement Transaction for Mutual Fund merchant")
    public void validateMasterRefundAllowedForNonInstantSettlementTxn(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_WO_SETTLEMENT_PAYMODE;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_WO_SETTLEMENT_PAYMODE.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setAggMid(AggrSettlementMerchant.getId())
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {
            Response refund = PGPHelpers.executeMasterRefund(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),"1.00",txnStatus.getResponse().getTXNID(),"");

            Assertions.assertThat(refund.jsonPath().getString("RESPCODE")).as("Result code incorrect for redundant refund")
                    .isEqualTo("601");

            Assertions.assertThat(refund.jsonPath().getString("RESPMSG")).as("Result message incorrect for Instant Settlement refund")
                    .contains("Refund request was raised for this transaction. But it is pending state");
        }

    }



    @Epic("Instant Settlement")
    @Feature("PGP-22762")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Instant Settlement Refund Handler Allowed for non Instant settlement Transaction for Mutual Fund merchant")
    public void validateRefundHandlerAllowedForNonInstantSettlementTxn(@Optional("true") Boolean isNativePlus) throws Exception {


        Constants.MerchantType InstantSettlementMerchant = Constants.MerchantType.INSTANT_WO_SETTLEMENT_PAYMODE;
        Constants.MerchantType AggrSettlementMerchant = Constants.MerchantType.AGGR_INSTANT_SETTLEMENT;

        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.INSTANT_WO_SETTLEMENT_PAYMODE.getId());
        }

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, InstantSettlementMerchant)
                .setRequestType("NATIVE_MF")
                .setTxnValue(ALLOWED_TXN_AMT)
                .setAggrMid(AggrSettlementMerchant.getId())
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(InstantSettlementMerchant, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.NET_BANKING)
                .setAggMid(AggrSettlementMerchant.getId())
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();



        Test:
        {

            RefundApi refundApi = new RefundApi(orderDTO.getMID(), InstantSettlementMerchant.getKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),"1.00",txnStatus.getResponse().getTXNID(),"");

            Response response = refundApi.execute();

            Assertions.assertThat(response.jsonPath().getString("RESPCODE")).as("Result code incorrect for redundant refund")
                    .isEqualTo("601");

            Assertions.assertThat(response.jsonPath().getString("RESPMSG")).as("Result message incorrect for Instant Settlement refund")
                    .contains("Refund request was raised for this transaction. But it is pending state");
        }

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-35638")
    @Parameters({"theme"})
    @Test(description = "Initiate refund for init state transaction and verify response of refund API")
    public void initRefundAsync(@Optional("enhancedwap_revamp") String theme) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("77")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateStatus("PENDING")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .AssertAll();

        Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),txnStatus.txnStatusResponse.getTXNID(), orderDTO.getTXN_AMOUNT(),
                "REFUND", "", null);

        String grepcmd = "grep -A 20 \"" + "ACQUIRING_INQUIRE_WITH_ACQ_ID" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"acquirementStatus\":\"INIT\"");

        Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("600");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-35638")
    @Parameters({"theme"})
    @Test(description = "Initiate refund for closed state transaction and verify response of refund API")
    public void closedRefundAsync(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly_PG2_Refund.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse("227", "Looks like OTP entered was incorrect. Please try again.")
                .AssertAll();

        Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                "REFUND", "", null);

        String grepcmd = "grep -A 20 \"" + "ACQUIRING_INQUIRE_WITH_ACQ_ID" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"acquirementStatus\":\"CLOSED\"");

        Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("600");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-35638")
    @Parameters({"isNativePlus"})
    @Test(description = "Initiate refund for init state native transaction and verify response of refund API")
    public void initAsyncRefund(@Optional("false") Boolean isNativePlus) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_PG2_Refund)
                .setRequestType("NATIVE")
                .setTxnValue("77.00")
                .build();

        String TxnToken  =  NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly_PG2_Refund, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateStatus("PENDING")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .AssertAll();

        Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(),Constants.MerchantType.getByMid(Constants.MerchantType.PGOnly_PG2_Refund.getId()).getKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                "REFUND", "", null);

        String grepcmd = "grep -A 20 \"" + "ACQUIRING_INQUIRE_WITH_ACQ_ID" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"acquirementStatus\":\"INIT\"");

        Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("600");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-35638")
    @Parameters({"isNativePlus"})
    @Test(description = "Initiate refund for closed state native transaction and verify response of refund API")
    public void closedAsyncRefund(@Optional("false") Boolean isNativePlus) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly_PG2_Refund)
                .setRequestType("NATIVE")
                .setTxnValue(ALLOWED_TXN_AMT)
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly_PG2_Refund, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateFailureResponse("227","Looks like OTP entered was incorrect. Please try again.")
                .AssertAll();

        Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(),Constants.MerchantType.getByMid(Constants.MerchantType.PGOnly_PG2_Refund.getId()).getKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "1.00",
                "REFUND", "", null);

        String grepcmd = "grep -A 20 \"" + "ACQUIRING_INQUIRE_WITH_ACQ_ID" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"acquirementStatus\":\"CLOSED\"");

        Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("600");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-35638")
    @Parameters({"theme"})
    @Test(description = "Initiate refund for init state UPI transaction and verify response of refund API")
    public void initUPIRefundAsync(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.PGOnly_PG2_Refund.getId());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("52")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateStatus("PENDING")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .AssertAll();

        Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(),txnStatus.txnStatusResponse.getTXNID(), orderDTO.getTXN_AMOUNT(),
                "REFUND", "", null);

        String grepcmd = "grep -A 20 \"" + "ACQUIRING_INQUIRE_WITH_ACQ_ID" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"acquirementStatus\":\"INIT\"");

        Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("600");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-35638")
    @Parameters({"theme"})
    @Test(description = "Initiate refund for closed state UPI transaction and verify response of refund API")
    public void closedUPIRefundAsync(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.HDFC_UPI_COLLECT_Pg2_Refund.getId());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HDFC_UPI_COLLECT_Pg2_Refund, theme)
                .setTXN_AMOUNT("99.41")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.HDFC_UPI_COLLECT_Pg2_Refund.getKey())
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse("227", "Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .AssertAll();

        Response response = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), orderDTO.getTXN_AMOUNT(),
                "REFUND", "", null);

        String grepcmd = "grep -A 20 \"" + "ACQUIRING_INQUIRE_WITH_ACQ_ID" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"" ;
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"acquirementStatus\":\"CLOSED\"");

        Assertions.assertThat(getAsyncRefundResultCode(response)).as("ResultCode mismatch").isEqualToIgnoringCase("600");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("RESULT_STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("RESULT_MSG mismatch").isEqualToIgnoringCase("Invalid refund request or restricted by bank");
    }

    @Feature("PGP-40234")
    @Owner(Constants.Owner.HARSHITA)
    @Parameters({"theme"})
    @Test(description = "Verify response for Master Refund Status API")
    public void masterRefundStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_PG2_Refund.getId());
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.AddnPay_PG2_Refund, theme)
                .setTXN_AMOUNT("50.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
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
                .validateBankName(orderDTO.getBANK_CODE())
                .validateCheckSum(Constants.MerchantType.AddnPay_PG2_Refund.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();

        Response refundResponse = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "10.00", "REFUND", "", null);

        MasterRefundStatusApi masterRefundStatusApi = new MasterRefundStatusApi(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = masterRefundStatusApi.executeUntilExpectedConditionMet("STATUS", "TXN_SUCCESS", 3, 12);

        Assertions.assertThat(response.jsonPath().get("RESPCODE").toString()).isEqualToIgnoringCase("10");
        Assertions.assertThat(response.jsonPath().get("STATUS").toString()).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().get("TXNID").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(response.jsonPath().get("ORDERID").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getORDERID());
        Assertions.assertThat(response.jsonPath().get("TXNAMOUNT").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getTXNAMOUNT());
        Assertions.assertThat(response.jsonPath().get("RESPMSG").toString()).isEqualToIgnoringCase("Refund Successfull");
        Assertions.assertThat(response.jsonPath().get("MID").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getMID());
        Assertions.assertThat(response.jsonPath().get("TXNDATE").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("REFUNDAMOUNT").toString()).isEqualToIgnoringCase("10.00");
        Assertions.assertThat(response.jsonPath().get("REFID").toString()).isEqualToIgnoringCase(refundResponse.jsonPath().get("body.refId").toString());
        Assertions.assertThat(response.jsonPath().get("REFUNDID").toString()).isEqualToIgnoringCase(refundResponse.jsonPath().get("body.refundId").toString());
        Assertions.assertThat(response.jsonPath().get("BANKTXNID").toString()).isEqualToIgnoringCase("");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.REFUND_TXN_ID").toString()).isEqualToIgnoringCase("["+refundResponse.jsonPath().get("body.refundId").toString()+"]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.BANK_TXN_ID").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.STATUS").toString()).isEqualToIgnoringCase("[TXN_SUCCESS]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.REFUNDAMOUNT").toString()).isEqualToIgnoringCase("[10.00]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.PAYMENTMODE").toString()).isEqualToIgnoringCase("[DC]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.CARD_ISSUER").toString()).isEqualToIgnoringCase("[HDFC Bank]");
    }

    @Feature("PGP-40234")
    @Owner(Constants.Owner.HARSHITA)
    @Parameters({"theme"})
    @Test(description = "Verify response for Refund Status API")
    public void refundStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_PG2_Refund.getId());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay_PG2_Refund, theme)
                .setTXN_AMOUNT("50.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validateBankName(orderDTO.getBANK_CODE())
                .validateCheckSum(Constants.MerchantType.AddnPay_PG2_Refund.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();

        Response refundResponse = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), orderDTO.getMerchantKey(),
                    orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "10.00", "REFUND", "", null);

        RefundStatusApi refundStatusApi = new RefundStatusApi(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = refundStatusApi.executeUntilExpectedConditionMet("REFUND_LIST.STATUS", "[TXN_SUCCESS]", 3, 12);

        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TXNID").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getTXNID()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getORDERID()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getTXNAMOUNT()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[Refund Successfull]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.MID").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getMID()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TXNDATE").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TOTALREFUNDAMT").toString()).isEqualToIgnoringCase("[10.00]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.REFUNDDATE").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[10]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.REFID").toString()).isEqualToIgnoringCase("["+refundResponse.jsonPath().get("body.refId").toString()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.REFUNDID").toString()).isEqualToIgnoringCase("["+refundResponse.jsonPath().get("body.refundId").toString()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.STATUS").toString()).isEqualToIgnoringCase("[TXN_SUCCESS]");
    }

    @Feature("PGP-40234")
    @Owner(Constants.Owner.HARSHITA)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify response for Refund Status API")
    public void refundStatus_01(@Optional("false") Boolean isNativePlus) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_PG2_Refund.getId());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay_PG2_Refund)
                .setRequestType("NATIVE")
                .setTxnValue("50.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay_PG2_Refund, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

       Response refundResponse = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), Constants.MerchantType.getByMid(Constants.MerchantType.AddnPay_PG2_Refund.getId()).getKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "10.00",
                "REFUND", "", null);

        RefundStatusApi refundStatusApi = new RefundStatusApi(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = refundStatusApi.executeUntilExpectedConditionMet("REFUND_LIST.STATUS", "[TXN_SUCCESS]", 3, 12);

        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TXNID").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getTXNID()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getORDERID()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getTXNAMOUNT()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[Refund Successfull]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.MID").toString()).isEqualToIgnoringCase("["+txnStatus.getResponse().getMID()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TXNDATE").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.TOTALREFUNDAMT").toString()).isEqualToIgnoringCase("[10.00]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.REFUNDDATE").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[10]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.REFID").toString()).isEqualToIgnoringCase("["+refundResponse.jsonPath().get("body.refId").toString()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.REFUNDID").toString()).isEqualToIgnoringCase("["+refundResponse.jsonPath().get("body.refundId").toString()+"]");
        Assertions.assertThat(response.jsonPath().get("REFUND_LIST.STATUS").toString()).isEqualToIgnoringCase("[TXN_SUCCESS]");
    }

    @Feature("PGP-40234")
    @Owner(Constants.Owner.HARSHITA)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify response for Master Refund Status API")
    public void masterRefundStatus_01(@Optional("false") Boolean isNativePlus) throws Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.AddnPay_PG2_Refund.getId());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay_PG2_Refund)
                .setRequestType("NATIVE")
                .setTxnValue("50.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay_PG2_Refund, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Response refundResponse = PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), Constants.MerchantType.getByMid(Constants.MerchantType.AddnPay_PG2_Refund.getId()).getKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "10.00",
                "REFUND", "", null);

        MasterRefundStatusApi refundStatusApi = new MasterRefundStatusApi(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = refundStatusApi.executeUntilExpectedConditionMet("STATUS", "TXN_SUCCESS", 3,12);
        Assertions.assertThat(response.jsonPath().get("RESPCODE").toString()).isEqualToIgnoringCase("10");
        Assertions.assertThat(response.jsonPath().get("STATUS").toString()).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().get("TXNID").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(response.jsonPath().get("ORDERID").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getORDERID());
        Assertions.assertThat(response.jsonPath().get("TXNAMOUNT").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getTXNAMOUNT());
        Assertions.assertThat(response.jsonPath().get("RESPMSG").toString()).isEqualToIgnoringCase("Refund Successfull");
        Assertions.assertThat(response.jsonPath().get("MID").toString()).isEqualToIgnoringCase(txnStatus.getResponse().getMID());
        Assertions.assertThat(response.jsonPath().get("TXNDATE").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("REFUNDAMOUNT").toString()).isEqualToIgnoringCase("10.00");
        Assertions.assertThat(response.jsonPath().get("REFID").toString()).isEqualToIgnoringCase(refundResponse.jsonPath().get("body.refId").toString());
        Assertions.assertThat(response.jsonPath().get("REFUNDID").toString()).isEqualToIgnoringCase(refundResponse.jsonPath().get("body.refundId").toString());
        Assertions.assertThat(response.jsonPath().get("BANKTXNID").toString()).isEqualToIgnoringCase("");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.REFUND_TXN_ID").toString()).isEqualToIgnoringCase("["+refundResponse.jsonPath().get("body.refundId").toString()+"]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.BANK_TXN_ID").toString()).isNotEmpty();
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.STATUS").toString()).isEqualToIgnoringCase("[TXN_SUCCESS]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.REFUNDAMOUNT").toString()).isEqualToIgnoringCase("[10.00]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.PAYMENTMODE").toString()).isEqualToIgnoringCase("[CC]");
        Assertions.assertThat(response.jsonPath().get("CHILD_REFUND_STATUS.CARD_ISSUER").toString()).isNotEmpty();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-43848")
    @Parameters({"theme"})
    @Test(description = "To verify response of /refund/api/v1/reversal for a successful txn.")
    public void reversal_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.CancelAllowed.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();
        RefundReversal cancel = new RefundReversal(mid, orderDTO.getORDER_ID());
        JsonPath response =  cancel.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.get("body.reversalId").toString()).isNotEmpty();
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("ACCEPTED"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("A_0000"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Reversal Request accepted"));
        softly.assertAll();
        //Will uncomment once changes are done on AWS
       /* String  reversalId = response.getString("body.reversalId");
        String grepcmd = "grep -A 20 \"" + "ACQUIRING_ORDER_CANCEL" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"";
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"cancelAction\":\"REFUND\"");

        String grepcmd1 = "grep -A 20 \"" + "alipayplus.fluxnet.paytm.bankcard.refund.request" + "\" /paytm/logs/instaproxy.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\"";
        String instaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd1);
        Assertions.assertThat(instaLogs).contains(reversalId); */

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-43848")
    @Parameters({"theme"})
    @Test(description = "To verify response of /refund/api/v1/reversal for a pending txn.")
    public void reversal_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.CancelAllowed.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, theme)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();
        RefundReversal cancel = new RefundReversal(mid, orderDTO.getORDER_ID());
        JsonPath response =  cancel.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.get("body.reversalId").toString()).isNotEmpty();
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("ACCEPTED"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("A_0000"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Reversal Request accepted"));
        softly.assertAll();
        //Will uncomment once changes are done on AWS
        /*String grepcmd = "grep -A 20 \"" + "ACQUIRING_ORDER_CANCEL" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"";
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"cancelAction\":\"ORDER_CLOSED\"");*/

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-43848")
    @Parameters({"theme"})
    @Test(description = "To verify response of /refund/api/v1/reversal for a refunded txn.")
    public void reversal_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.CancelAllowed.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();
        String txnId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID);
        PGPHelpers.initiateAsyncRefund(orderDTO.getMID(), Constants.MerchantType.getByMid(mid).getKey(),
                orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnId, "10.00",
                "REFUND", "", null);
        RefundReversal cancel = new RefundReversal(mid, orderDTO.getORDER_ID());
        JsonPath response =  cancel.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("ACCEPTED"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("A_0000"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Reversal Request accepted"));
        softly.assertAll();
        //Will uncomment once changes are done on AWS
        /*String grepcmd = "grep -A 20 \"" + "ACQUIRING_ORDER_CANCEL" + "\" /paytm/logs/refund_facade.log | "
                + "grep -A 20 \"" + orderDTO.getORDER_ID() + "\" | grep -A 20 \"" + "RESPONSE" + "\"";
        String facadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.REFUND_SERVICE, grepcmd);
        Assertions.assertThat(facadeLogs).contains("\"resultCode\":\"REFUND_EXIST_ON_ORDER\"");*/
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-853")
    @Test(description = "When Refund Amount is Greater Than Amount Set At Payments Refund Desitantion Should Be Null")
    public void WhenRefundAmountIsGreaterThanAmountSetAtPaymentsRefundDesitantionShouldBeNull() throws Exception{
        // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        // Create InitTxnDTO with the provided parameters
        String custId = "Test" + CommonHelpers.generateOrderId();
        String orderId = "test" + CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .setCustId(custId)
                .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                .setWebsiteName("retail")
                .setOrderId(orderId)
                .build();
        
        // Execute the initiate transaction
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        // Create Process Transaction Request with UPI_INTENT
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalProperty("payerCmid", "8006006993");
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("USRPWD")
                .setQrImageRequired(false)
                .setSeqNumber("PYTM0123456")
                .setExtendInfo(extendInfo)
                .build();

        // Execute Process Transaction
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
        // Extract deeplink information using QRHelper
        Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
        String transactionRef = deeplinkInfo.get("tr");
        String amountFromDeeplink = deeplinkInfo.get("amount");
        String payeeVpa = deeplinkInfo.get("pa");

        // Execute PTYBLIIntentCallback to complete the UPI Intent transaction
        PTYBLIIntentCallback ptybliIntentCallback = new PTYBLIIntentCallback();
        ptybliIntentCallback.buildRequest("Abhishek Verma", LocalConfig.PGP_HOST, payeeVpa, transactionRef, "payervpa@test");
        JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
        
        // Validate callback response
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ptybliCallbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
        softly.assertAll();

        // Get Payment Status with retry until TXN_SUCCESS
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
        JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
        // Validate payment status
        SoftAssertions paymentAssertions = new SoftAssertions();
        paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        paymentAssertions.assertAll();

        // Get txnId from payment status response
        String txnId = paymentStatusJsonPath.getString("body.txnId");

        // Initiate Async Refund with agentInfo (using reqSpecAsyncRiskInfo which has clientId:"" and correct agentInfo)
        SyncRefund syncRefund = new SyncRefund();
        Response refundResponse = given().spec(syncRefund.reqSpecAsyncRiskInfo(merchant, "16", orderId, txnId))
                .post().then().extract().response();

        // Validate refund response
        JsonPath refundJsonPath = refundResponse.jsonPath();
        SoftAssertions refundAssertions = new SoftAssertions();
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        refundAssertions.assertAll();

        // Verify in insta logs that refundDestination is null when function is pg.router.paytm.upi.refund.request
        String instaLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "pg.router.paytm.upi.refund.request");
        Assertions.assertThat(instaLogs).contains("\"refundDestination\":\"null\"");

    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-853")
    @Test(description = "When Refund Amount is Greater Than Amount Set At Router It Should Be Axis Bank Transfer")
    public void WhenRefundAmountIsGreaterThanAmountSetAtRouterItShouldBeAxisBankTransfer() throws Exception{
        // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        // Create InitTxnDTO with the provided parameters
        String custId = "Test" + CommonHelpers.generateOrderId();
        String orderId = "test" + CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .setCustId(custId)
                .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                .setWebsiteName("retail")
                .setOrderId(orderId)
                .build();
        
        // Execute the initiate transaction
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        // Create Process Transaction Request with UPI_INTENT
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalProperty("payerCmid", "8006006993");
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("USRPWD")
                .setQrImageRequired(false)
                .setSeqNumber("PYTM0123456")
                .setExtendInfo(extendInfo)
                .build();

        // Execute Process Transaction
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
        // Extract deeplink information using QRHelper
        Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
        String transactionRef = deeplinkInfo.get("tr");
        String amountFromDeeplink = deeplinkInfo.get("amount");
        String payeeVpa = deeplinkInfo.get("pa");

        // Execute PTYBLIIntentCallback to complete the UPI Intent transaction
        PTYBLIIntentCallback ptybliIntentCallback = new PTYBLIIntentCallback();
        ptybliIntentCallback.buildRequest("Abhishek Verma", LocalConfig.PGP_HOST, payeeVpa, transactionRef, "payervpa@test");
        JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
        
        // Validate callback response
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ptybliCallbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
        softly.assertAll();

        // Get Payment Status with retry until TXN_SUCCESS
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
        JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
        // Validate payment status
        SoftAssertions paymentAssertions = new SoftAssertions();
        paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        paymentAssertions.assertAll();

        // Get txnId from payment status response
        String txnId = paymentStatusJsonPath.getString("body.txnId");

        // Initiate Async Refund with agentInfo (using reqSpecAsyncRiskInfo which has clientId:"" and correct agentInfo)
        SyncRefund syncRefund = new SyncRefund();
        Response refundResponse = given().spec(syncRefund.reqSpecAsyncRiskInfo(merchant, "10.4", orderId, txnId))
                .post().then().extract().response();

        // Validate refund response
        JsonPath refundJsonPath = refundResponse.jsonPath();
        SoftAssertions refundAssertions = new SoftAssertions();
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        refundAssertions.assertAll();

        // Verify in insta logs that function is pg.router.paytm.banktransfer.refund.request and bankAbbr is AXIS
        String instaLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "pg.router.paytm.banktransfer.refund.request");
        Assertions.assertThat(instaLogs).contains("\"function\":\"pg.router.paytm.banktransfer.refund.request\"");
        Assertions.assertThat(instaLogs).contains("\"bankAbbr\":\"AXIS\"");

    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-853")
    @Test(description = "When Refund Amount is Lesser Than Amount Set At Router It Should Be VPA")
    public void WhenRefundAmountIsLesserThanAmountSetAtRouterRefundDestinationShouldBeVPA() throws Exception{
        // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        // Create InitTxnDTO with the provided parameters
        String custId = "Test" + CommonHelpers.generateOrderId();
        String orderId = "test" + CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .setCustId(custId)
                .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                .setWebsiteName("retail")
                .setOrderId(orderId)
                .build();
        
        // Execute the initiate transaction
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        // Create Process Transaction Request with UPI_INTENT
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalProperty("payerCmid", "8006006993");
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("USRPWD")
                .setQrImageRequired(false)
                .setSeqNumber("PYTM0123456")
                .setExtendInfo(extendInfo)
                .build();

        // Execute Process Transaction
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
        // Extract deeplink information using QRHelper
        Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
        String transactionRef = deeplinkInfo.get("tr");
        String amountFromDeeplink = deeplinkInfo.get("amount");
        String payeeVpa = deeplinkInfo.get("pa");

        // Execute PTYBLIIntentCallback to complete the UPI Intent transaction
        PTYBLIIntentCallback ptybliIntentCallback = new PTYBLIIntentCallback();
        ptybliIntentCallback.buildRequest("Abhishek Verma", LocalConfig.PGP_HOST, payeeVpa, transactionRef, "payervpa@test");
        JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
        
        // Validate callback response
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ptybliCallbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
        softly.assertAll();

        // Get Payment Status with retry until TXN_SUCCESS
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
        JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
        // Validate payment status
        SoftAssertions paymentAssertions = new SoftAssertions();
        paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        paymentAssertions.assertAll();

        // Get txnId from payment status response
        String txnId = paymentStatusJsonPath.getString("body.txnId");

        // Initiate Async Refund with agentInfo (using reqSpecAsyncRiskInfo which has clientId:"" and correct agentInfo)
        SyncRefund syncRefund = new SyncRefund();
        Response refundResponse = given().spec(syncRefund.reqSpecAsyncRiskInfo(merchant, "9.4", orderId, txnId))
                .post().then().extract().response();

        // Validate refund response
        JsonPath refundJsonPath = refundResponse.jsonPath();
        SoftAssertions refundAssertions = new SoftAssertions();
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        refundAssertions.assertAll();

        // Verify in insta logs that function is pg.router.paytm.upi.refund.request and refundDestination is VPA
        String instaLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "pg.router.paytm.upi.refund.request");
        Assertions.assertThat(instaLogs).contains("\"function\":\"pg.router.paytm.upi.refund.request\"");
        Assertions.assertThat(instaLogs).contains("\"refundDestination\":\"VPA\"");

    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-853")
    @Test(description = "When Refund Amount is Lesser Than Amount Set At Payments And Payer VPA has Paytm Handler It Should be Bank Transfer AXIS")
    public void WhenRefundAmountIsLesserThanAmountSetAtPaymentsAndPayerVPAhasPaytmHandlerItShouldbeBankTransferAXIS() throws Exception{
        // AI-Generated: 2025-01-02 - Refactoring: Updated to use all caps MID constant
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        // Create InitTxnDTO with the provided parameters
        String custId = "Test" + CommonHelpers.generateOrderId();
        String orderId = "test" + CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .setCustId(custId)
                .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                .setWebsiteName("retail")
                .setOrderId(orderId)
                .build();
        
        // Execute the initiate transaction
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();

        // Create Process Transaction Request with UPI_INTENT
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalProperty("payerCmid", "8006006993");
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken, orderId)
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("USRPWD")
                .setQrImageRequired(false)
                .setSeqNumber("PYTM0123456")
                .setExtendInfo(extendInfo)
                .build();

        // Execute Process Transaction
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
                String deeplink = processTxnV1Response.getBody().getDeepLinkInfo().getDeepLink();
        // Extract deeplink information using QRHelper
        Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
        String transactionRef = deeplinkInfo.get("tr");
        String amountFromDeeplink = deeplinkInfo.get("amount");
        String payeeVpa = deeplinkInfo.get("pa");

        // Execute PTYBLIIntentCallback to complete the UPI Intent transaction
        PTYBLIIntentCallback ptybliIntentCallback = new PTYBLIIntentCallback();
        ptybliIntentCallback.buildRequest("Abhishek Verma", LocalConfig.PGP_HOST, payeeVpa, transactionRef, "payervpa@ptyes");
        JsonPath ptybliCallbackResponse = ptybliIntentCallback.execute().jsonPath();
        
        // Validate callback response
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ptybliCallbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
        softly.assertAll();

        // Get Payment Status with retry until TXN_SUCCESS
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
                merchant.getId(),
                orderId);
        JsonPath paymentStatusJsonPath = getPaymentStatus.executeUntilExpectedConditionMet(
                "body.resultInfo.resultStatus", "TXN_SUCCESS", 5, 12).jsonPath();
        
        // Validate payment status
        SoftAssertions paymentAssertions = new SoftAssertions();
        paymentAssertions.assertThat(paymentStatusJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_SUCCESS");
        paymentAssertions.assertAll();

        // Get txnId from payment status response
        String txnId = paymentStatusJsonPath.getString("body.txnId");

        // Initiate Async Refund with agentInfo (using reqSpecAsyncRiskInfo which has clientId:"" and correct agentInfo)
        SyncRefund syncRefund = new SyncRefund();
        Response refundResponse = given().spec(syncRefund.reqSpecAsyncRiskInfo(merchant, "9.4", orderId, txnId))
                .post().then().extract().response();

        // Validate refund response
        JsonPath refundJsonPath = refundResponse.jsonPath();
        SoftAssertions refundAssertions = new SoftAssertions();
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        refundAssertions.assertThat(refundJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        refundAssertions.assertAll();

        // Verify in insta logs that function is pg.router.paytm.banktransfer.refund.request and bankAbbr is AXIS
        String instaLogs = LogsValidationHelper.verifyLogsOnPod("INSTAPROXY", orderId, "pg.router.paytm.banktransfer.refund.request");
        Assertions.assertThat(instaLogs).contains("\"function\":\"pg.router.paytm.banktransfer.refund.request\"");
        Assertions.assertThat(instaLogs).contains("\"bankAbbr\":\"AXIS\"");

    }
}