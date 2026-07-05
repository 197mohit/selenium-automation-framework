package scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.ServerConfigProvider;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.wallet.WalletPaymentConfirmation;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.MerchantManager;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.DefaultCommission;
import com.paytm.utils.merchant.merchant.ExistingMerchantContract;
import com.paytm.utils.merchant.merchant.Merchant;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import groovy.json.JsonSlurper;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.apphelpers.PGPHelpers.*;
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum;
import static groovy.json.JsonOutput.toJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Owner("Gagandeep")
public class Refund extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final String postConvFlag = "";
    private static final String TXN_SUCCESS = "TXN_SUCCESS";
    private static final String TXN_FAILURE = "TXN_FAILURE";
    private static final String PENDING = "PENDING";
    private static final String invalidRefundMsg = "Invalid refund request or restricted by bank";
    private static final String invalidRefundMsg_failtxn = "Invalid refund request. Kindly retry once original order is successful.";


    @Step("Validate Initiate Refund for status: {1}")
    private void validateInitiateRefundResult(Response response, String status) {
        Assertions.assertThat(response.jsonPath().get("STATUS").toString()).as("TXN_STATUS mismatch").isEqualTo(status);
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of PGOnly CC transaction with transaction amount as whole number.", groups = "smoke")
    public void successfulPGOnlyCCRefund(@Optional("enhancedweb") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
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


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }







    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of PGOnly DC transaction with transaction amount in decimal number.")
    public void successfulPGOnlyDCRefund(@Optional("enhancedweb") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT("10.50").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of walletOnly transaction")
    public void successfulWalletOnlyRefund(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.WalletOnly_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();
        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of PGOnly UPI transaction")
    public void successfulPGOnlyUPIRefund(@Optional("merchant4") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of Hybrid Transaction.")
    public void successfulHybridRefund(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {

            validateRefundAllowedWithChecksum(MerchantType.REFUND_HYBRID.getId());
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
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.REFUND_HYBRID, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText("1");
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();


        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            RefundStatusHelper refundStatusHelper = getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            map = refundStatusHelper.getRefundBy("PAYMENTMODE", "CC");
            refundStatusHelper.validate(map, "GATEWAY", "HDFC");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.assertAll();
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of Add N Pay transaction")
    public void successfulAddNPayRefund(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.AddnPay_PG2_Refund.getId());
            User user = userManager.getForRead(Label.BASIC);
            OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_Refund, theme, user)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_Refund, theme, user)
                .setTXN_AMOUNT("3.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateStatus(TXN_SUCCESS, 1)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund.")
    public void successfulPartialRefund(@Optional("merchant4") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), "1.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateTOTALREFUNDAMT("1.00", 0)
                    .validateREFUNDAMOUNT("1.00", 0)
                    .assertAll();
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify refund failure of the transaction whose refund has already been succeeded.")
    public void verifyRefundFailureOfAlreadySuccessfulRefund(@Optional("enhancedweb_revamp") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
            OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                    .setTXN_AMOUNT("10.00").build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
        getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                .validateStatus(TXN_SUCCESS, 0)
                .assertAll();

        validateSecondRefundRequest:
        {
            Response response = initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID() + "1234", orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(response.jsonPath().get("RESPCODE").toString()).as("RESPCODE mismatch").isEqualToIgnoringCase("619");
            softly.assertThat(response.jsonPath().get("RESPMSG").toString()).as("RESPMSG mismatch").isEqualToIgnoringCase("Invalid refund amount.");
            softly.assertThat(response.jsonPath().get("STATUS").toString()).as("STATUS mismatch").isEqualToIgnoringCase("TXN_FAILURE");
            softly.assertAll();
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify refund failure of failed PGonly Transaction.")
    public void verifyRefundFailureOfFailedPGOnlyTxn(@Optional("enhancedwap_revamp") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateFailureResponse(ResponseCode.BANK_TXN_FAILURE.getRespCode(), ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();

        Test:
        {
            Response response = initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            validateInitiateRefundResult(response, TXN_FAILURE);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(response.jsonPath().get("RESPCODE").toString()).isEqualToIgnoringCase("679");
            softly.assertThat(response.jsonPath().get("RESPMSG").toString()).isEqualToIgnoringCase(invalidRefundMsg_failtxn);
            softly.assertAll();
        }


    }

    @Parameters({"theme"})
    @Test(description = "Verify the successful refund of txn for which a partial refund was already succeeded but the total refund amount doesn't exceed the actual txn amount")
    public void successfulMultiplePartialRefund(@Optional("merchant4") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), "1.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT(), 0)
                    .validateTOTALREFUNDAMT("1.00", 0)
                    .validateREFUNDAMOUNT("1.00", 0)
                    .assertAll();
            String refId = orderDTO.getORDER_ID() + "1212";
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), refId, "2.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), refId, true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT(), 0)
                    .validateTOTALREFUNDAMT(orderDTO.getTXN_AMOUNT(), 0)
                    .validateREFUNDAMOUNT("2.00", 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify refund failure of txn for which a partial refund was already succeeded but the total refund amount exceeds the actual txn amount.")
    public void failurePartilRefundDoneTwiceThatExceedsTxnAmount(@Optional("merchant4") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), "1.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateREFUNDAMOUNT("1.00", 0)
                    .assertAll();

            Response refundApi = initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID() + "124", "5.00", txnStatus.getResponse().getTXNID(), postConvFlag);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(refundApi.jsonPath().get("RESPCODE").toString()).isEqualToIgnoringCase("619");
            softly.assertThat(refundApi.jsonPath().get("RESPMSG").toString()).isEqualToIgnoringCase("Invalid refund amount.");
            softly.assertThat(refundApi.jsonPath().get("STATUS").toString()).isEqualToIgnoringCase(TXN_FAILURE);
            softly.assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of base amount in post convenience transaction when CF remain same.")
    public void successfulRefundOfBaseAmountInPostConvenienceTxn(@Optional("merchant4") String theme) throws PGPException {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            validateRefundAllowedWithChecksum(mid);
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
            cashierPage.payBy(PayMode.CC);
            TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                    .validateSuccessResponse()
                    .AssertAll();

            initiateRefundRequest(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), order.getORDER_ID(), order.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "C");
            getRefundStatus(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();

        }
    }

    @Parameters({"theme"})
    @Test(description = "Parameter validation: Verify refund failure with incorrect mid, orderID and TxnId")
    public void refundFailureWithIncorrectMidOrderIdTxnId(@Optional("merchant4") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        InvalidMidValidation:
        {
            Response response = initiateRefundRequest(orderDTO.getMID() + "a", orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            validateInitiateRefundResult(response, PENDING);
            Assertions.assertThat(response.jsonPath().get("RESPCODE").toString()).as("RESPCODE mismatch").isEqualToIgnoringCase("501");
            Assertions.assertThat(response.jsonPath().get("RESPMSG").toString()).as("RESPMSG mismatch").isEqualToIgnoringCase("System Error.");
        }
        InvalidOrderIdValidation:
        {
            Response response = initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID() + "ab", orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            validateInitiateRefundResult(response, TXN_FAILURE);
            Assertions.assertThat(response.jsonPath().get("RESPCODE").toString()).as("RESPCODE mismatch").isEqualToIgnoringCase("627");
            Assertions.assertThat(response.jsonPath().get("RESPMSG").toString()).as("RESPMSG mismatch").isEqualToIgnoringCase("Order Details Mismatch");
        }
        InvalidTxnIdValidation:
        {
            Response response = initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID() + "129", postConvFlag);
            validateInitiateRefundResult(response, TXN_FAILURE);
            Assertions.assertThat(response.jsonPath().get("RESPCODE").toString()).as("RESPCODE mismatch").isEqualToIgnoringCase("617");
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify wallet refund when wallet amount is already breaching wallet limits", groups = Group.Status.TO_BE_FIXED)
    public void successfulRefundForWalletLimitBreach(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.WalletOnly.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        try {
            WalletHelpers.breachAddMoneyLimit(user);

            Test:
            {
                initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
                getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                        .validateStatus(TXN_SUCCESS, 0)
                        .assertAll();
            }
        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of base amount with fee in post convenience transaction when CF remain same.")
    public void successfulRefundOfBaseAmountWithFeeInPostConvenienceTxn(@Optional("merchant4") String theme) throws PGPException {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            validateRefundAllowedWithChecksum(mid);
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
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        double commissionAmount = convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
        String totalRefundAmount = String.valueOf(commissionAmount + Double.valueOf(order.getTXN_AMOUNT())).subSequence(0, 4).toString();

        Test:
        {
            initiateRefundRequest(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), order.getORDER_ID() + "abcd", "1.00", txnStatus.getResponse().getTXNID(), "R");
            getRefundStatus(order.getMID(), order.getMerchantKey(), order.getORDER_ID() + "abcd", true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateREFUNDAMOUNT("1.00", 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund of base amount in post convenience transaction when CF remain same.")
    public void successfulPartialRefundOfBaseAmountInPostConvenienceTxn(@Optional("enhancedweb") String theme) throws PGPException {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            validateRefundAllowedWithChecksum(mid);
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
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String partialRefundAmount = String.valueOf(Double.valueOf(order.getTXN_AMOUNT()) - 1.00);

        Test:
        {
            initiateRefundRequest(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), order.getORDER_ID(), partialRefundAmount, txnStatus.getResponse().getTXNID(), "C");
            getRefundStatus(order.getMID(), order.getMerchantKey(), order.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful refund for Subs First Request via CC")
    public void verifySuccessfulRefundOnSubsViaCC(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.Subscription_PGOnly.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();

        if (executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID()).isEmpty())
            throw new SkipException("Acquirement Id not found, subscription is not successful");
        if (getSavedCardId(subsId).isEmpty())
            throw new SkipException("Saved Card Id not found");

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }

    }

    @Parameters({"theme"})
    @Test(description = "To verify successful refund for Subs Renewal Txn")
    public void verifySuccessfulRefundOfSubsRenewalTxn(@Optional("merchant4") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.Subscription_PGOnly.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();


        Assertions.assertThat(executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        Assertions.assertThat(getSavedCardId(subsId)).isNotNull().isNotEmpty();
        /*Renew Subs*/
        String orderIdForRenew = CommonHelpers.generateOrderId();
        String respMsg = renewSubscription(orderIdForRenew, orderDTO.getMID(), Double.valueOf(orderDTO.getTXN_AMOUNT()), subsId);
        Assertions.assertThat(respMsg).isEqualToIgnoringCase("Subscription Txn accepted.");
        Assertions.assertThat(executeUntilAcquirementIdNotFound(subsId, orderIdForRenew)).isNotNull().isNotEmpty();

        initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderIdForRenew, orderIdForRenew, orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
        getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderIdForRenew, true)
                .validateStatus(TXN_SUCCESS, 0)
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate PPBL Refund on EnhnacedNative")
    public void validatePPBLRefund(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBLYONLY_PG2_Refund, theme, user).
                setTXN_AMOUNT("100.0").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxPPBLPassCode().clearAndType(new PaymentDTO().getPasscode());
        cashierPage.buttonPpblSumbit().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        ProcessRefund:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus("TXN_SUCCESS", 0)
                    .assertAll();
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify successful SYNC refund of PGOnly CC transaction with transaction amount as whole number.")
    public void successfulPGOnlyCCSyncRefund(@Optional("enhancedweb") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
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


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .spec(syncRefund.refundDetailSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("CREDIT_CARD"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "orderId", equalTo(orderDTO.getORDER_ID()));

        }

    }


    @Parameters({"theme"})
    @Test(description = "Verify successful SYNC refund of PGOnly DC transaction with transaction amount as whole number.")
    public void successfulPGOnlyDCSyncRefund(@Optional("enhancedweb") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
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


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .spec(syncRefund.refundDetailSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("DEBIT_CARD"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "orderId", equalTo(orderDTO.getORDER_ID()));

        }


    }

    @Parameters({"theme"})
    @Test(description = "Verify successful Sync refund for walletOnly transaction")
    public void successfulWalletOnlySyncRefund(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.WalletOnly_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(MerchantType.WalletOnly_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.WalletOnly_PG2_Refund, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("BALANCE"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful Sync refund of PGOnly UPI transaction")
    public void successfulPGOnlyUPISyncRefund(@Optional("enhancedweb") String theme) throws PGPException {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("UPI"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify successful Sync refund of Hybrid Transaction.")
    public void successfulHybridSyncRefund(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.REFUND_HYBRID.getId());
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
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.REFUND_HYBRID, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFCBANK.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();


        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            ValidatableResponse response = given().spec(syncRefund.reqSpec(MerchantType.REFUND_HYBRID, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .spec(syncRefund.refundDetailSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("CREDIT_CARD"),
                            "refundDetailInfoList.payMethod", hasItem("BALANCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "orderId", equalTo(orderDTO.getORDER_ID()));


            SoftAssert softAssert = new SoftAssert();
            ArrayList<Map<String, String>> refundDetails = response.extract().response().jsonPath().get("body.refundDetailInfoList");
            HashMap CreditRefund = (HashMap) refundDetails.get(0);
            HashMap WalletRefund = (HashMap) refundDetails.get(1);
            softAssert.assertEquals(CreditRefund.get("refundAmount"), "1.00");
            softAssert.assertEquals(CreditRefund.get("payMethod"), "CREDIT_CARD");
            softAssert.assertEquals(CreditRefund.get("refundType"), "TO_SOURCE");
            softAssert.assertEquals(WalletRefund.get("refundAmount"), "1.00");
            softAssert.assertEquals(WalletRefund.get("payMethod"), "BALANCE");
            softAssert.assertEquals(WalletRefund.get("refundType"), "TO_SOURCE");
            softAssert.assertAll();


        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful Sync refund of Add N Pay transaction")
    public void successfulAddNPaySyncRefund(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.AddnPay_PG2_Refund.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay_PG2_Refund, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnStatus.txnStatusResponse.TXNID);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.AddnPay_PG2_Refund, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("BALANCE"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }
    @Parameters({"theme"})
    @Test(description = "Verify successful Sync refund of base amount in post convenience transaction")
    public void successfulRefundOfBaseAmountInPostConvenienceTxn_Sync(@Optional("enhancedweb_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PCF_SYNC_REFUND.getId());
        }
        // commented the Edit merchant API already configured flatcomission of 1.15 on merchant
        /*double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(MerchantType.PCF_SYNC_REFUND.getId());
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        } */

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PCF_SYNC_REFUND, theme).setTXN_AMOUNT("10.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            Map<String,Map> ReqMap = (Map)new JsonSlurper().parseText(syncRefund.request);
            ReqMap.get("body").put("txnType","R");
            Map BodyMap = (Map)new JsonSlurper().parseText(syncRefund.body);
            BodyMap.put("txnType","R");
            syncRefund.body= new ObjectMapper().writeValueAsString(BodyMap);
            syncRefund.request = new ObjectMapper().writeValueAsString(ReqMap);
            given().spec(syncRefund.reqSpec(MerchantType.PCF_SYNC_REFUND, orderDTO.getTXN_AMOUNT(), orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("CREDIT_CARD"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo("10.00"),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund of base amount in post convenience transaction when CF remain same.")
    public void successfulPartialRefundOfBaseAmountInPostConvenienceTxn_async(@Optional("enhancedweb_revamp") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PCF_SYNC_REFUND.getId());
        }
        // commented the Edit merchant API already configured flatcomission of 1.15 on merchant
        /* double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(MerchantType.PCF_SYNC_REFUND.getId());
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        } */

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PCF_SYNC_REFUND, theme)
                .setTXN_AMOUNT("3.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String partialRefundAmount = String.valueOf(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PCF_SYNC_REFUND, partialRefundAmount, orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("CREDIT_CARD"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo("2.00"),
                            "orderId", equalTo(orderDTO.getORDER_ID()));

        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify successful partial refund.")
    public void successfulPartialRefund_async(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
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
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("CREDIT_CARD"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo("1.00"),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify paytm checksum mismatch if checksum is invalid")
    public void invalidChecksumMsgForSyncRefund(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            syncRefund.checksum = getChecksum(MerchantType.ADD_DAILY.getKey(), toJson(orderDTO.toString()));
            JsonPath response = given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then().extract().body().jsonPath();

            Assertions.assertThat(response.getString("body.resultInfo.resultStatus"))
                    .isEqualTo("TXN_FAILURE").as("Invalid Checksum still getting passed");
            Assertions.assertThat(response.getString("body.resultInfo.resultCode"))
                    .isEqualTo("330").as("Invalid ResultCode");
            Assertions.assertThat(response.getString("body.resultInfo.resultMsg"))
                    .isEqualTo("Paytm checksum mismatch.").as("Invalid Checksum still getting passed");
        }
    }
    @Parameters({"theme"})
    @Test(description = "Verify merchant Id not coming in request")
    public void verifyMerchantIdNotComingInReq(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            syncRefund.request = syncRefund.request.replace("{MID}","");
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .root("body.resultInfo")
                    .body("resultStatus", equalTo("TXN_FAILURE"),
                            "resultCode", equalTo("335"),
                            "resultMsg", equalTo("Invalid merchant Id."));
        }
    }
    @Parameters({"theme"})
    @Test(description = "Verify amount is not coming in request")
    public void verifyAmountNotComingInReq(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            syncRefund.request = syncRefund.request.replace("{TRANSACTION_AMOUNT}"," ");
            JsonPath response =   given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then().extract().jsonPath();

            Assertions.assertThat(response.getString("body.resultInfo.resultStatus"))
                    .isEqualTo("TXN_FAILURE").as("Invalid Checksum still getting passed");
            Assertions.assertThat(response.getString("body.resultInfo.resultCode"))
                    .isEqualTo("330").as("Invalid ResultCode");
            Assertions.assertThat(response.getString("body.resultInfo.resultMsg"))
                    .isEqualTo("Paytm checksum mismatch.").as("Invalid Checksum still getting passed");
        }
    }
    @Parameters({"theme"})
    @Test(description = "Verify Invalid refund amount when refund amount greater than txn ")
    public void verifyRefundAmountGreaterThanInTxnAmount(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "2.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .root("body.resultInfo")
                    .body("resultStatus", equalTo("TXN_FAILURE"),
                            "resultCode", equalTo("674"),
                            "resultMsg", equalTo("Refund amount is invalid or greater than transaction amount"));
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify OrderID not same as Txn OrderId")
    public void verifyOrdderIdNotSameAsTxn(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", CommonHelpers.generateOrderId(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .root("body.resultInfo")
                    .body("resultStatus", equalTo("TXN_FAILURE"),
                            "resultCode", equalTo("627"),
                            "resultMsg", equalTo("Order Details Mismatch"));
        }
    }



    @Parameters({"theme"})
    @Test(description = "Verify Partial refund amount less than 1")
    public void verifyPartialAmountLessThanOne(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "0.65", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .root("body")
                    .body("resultInfo.resultStatus", equalTo("TXN_FAILURE"),
                            "resultInfo.resultCode", equalTo("635"),
                            "resultInfo.resultMsg", equalTo("Partial Refund under Rupee 1 is not allowed."),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }
// The instant refund Channel code is defined by Alipay for our MOCK we need FISB bank to make Instant refund
//    @Issue("SMP1-5344")
//    @Parameters({"theme"})
//    @Test(description = "Verify refund destination refundType To_INSTANT",enabled = false)
    public void verifyAmountToDestinationToInstant(@Optional("merchant4") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.REFUND_IMPSPGONLY.getId());
        }
        User user = new User("7000100110","paytm@123");
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.REFUND_IMPSPGONLY, theme).setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();

            Map<String,Map> ReqMap = (Map)new JsonSlurper().parseText(syncRefund.request);
            ReqMap.get("body").put("preferredDestination","TO_INSTANT");
            ReqMap.get("body").put("comments","comment");
            Map<String,String> BodyMap = (Map)new JsonSlurper().parseText(syncRefund.body);
            BodyMap.put("preferredDestination","TO_INSTANT");
            BodyMap.put("comments","comment");
            syncRefund.body= new ObjectMapper().writeValueAsString(BodyMap);
            syncRefund.request = new ObjectMapper().writeValueAsString(ReqMap);
            given().spec(syncRefund.reqSpec(MerchantType.REFUND_IMPSPGONLY, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("DEBIT_CARD"),
                            "refundDetailInfoList.refundType", hasItem("TO_INSTANT"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo("1.00"),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }
    @Parameters({"theme"})
    @Test(description = "Verify refund destination refundType To_SOURCE when IMPS merchant but CC txn")
    public void verifyAmountToDestinationToSourceIfIMPSMerchantButCCtxn(@Optional("enhancedweb") String theme) throws Exception {

        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.REFUND_IMPSPGONLY_PG2.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.REFUND_IMPSPGONLY_PG2, theme).setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.REFUND_IMPSPGONLY_PG2, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund.resultSchema())
                    .spec(syncRefund.refundDetailSchema())
                    .root("body")
                    .body("refundDetailInfoList.payMethod", hasItem("CREDIT_CARD"),
                            "refundDetailInfoList.refundType", hasItem("TO_SOURCE"),
                            "txnAmount", equalTo(orderDTO.getTXN_AMOUNT()),
                            "refundAmount", equalTo("1.00"),
                            "orderId", equalTo(orderDTO.getORDER_ID()));
        }
    }




    @Parameters({"theme"})
    @Test(description = "Verify refund failure of the transaction whose sync refund has already been succeeded.")
    public void verifyRefundFailureOfAlreadySuccessfulRefund_sync(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).build();
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
        {   SyncRefund syncRefund_1 = new SyncRefund();
            given().spec(syncRefund_1.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund_1.resultSchema())
                    .spec(syncRefund_1.refundDetailSchema());

            SyncRefund syncRefund_2 = new SyncRefund();
            given().spec(syncRefund_2.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then().root("body.resultInfo")
                    .body("resultStatus", equalTo("TXN_FAILURE"),
                        "resultCode", equalTo("619"),
                        "resultMsg", equalTo("Invalid refund amount."));

        }


    }

    @Parameters({"theme"})
    @Test(description = "Verify refund failure of failed PGonly Transaction.")
    public void verifyRefundFailureOfFailedPGOnlyTxn_sync(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
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
            SyncRefund syncRefund = new SyncRefund();
            given().spec(syncRefund.reqSpec(MerchantType.PGOnly_PG2_Refund, "1.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then().root("body.resultInfo")
                    .body("resultStatus", equalTo("TXN_FAILURE"),
                            "resultCode", equalTo("679"),
                            "resultMsg", equalTo(invalidRefundMsg_failtxn));

        }
    }

    @Parameters({"theme"})
    @Test(description = "To validate partial refundfor 5 Rs twice when the txn amount is 11 rs")
    public void partialRefundRefundTwiceForSyncRefund(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme)
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
            SyncRefund syncRefund_1 = new SyncRefund();
            given().spec(syncRefund_1.reqSpec(MerchantType.PGOnly_PG2_Refund, "5.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund_1.resultSchema())
                    .spec(syncRefund_1.refundDetailSchema());

            SyncRefund syncRefund_2 = new SyncRefund();
            given().spec(syncRefund_2.reqSpec(MerchantType.PGOnly_PG2_Refund, "5.00", orderDTO.getORDER_ID(),
                    txnStatus.getResponse().getTXNID()))
                    .post().then()
                    .spec(syncRefund_1.resultSchema())
                    .spec(syncRefund_1.refundDetailSchema());


        }

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify OnPaytm=true in promo payload in queue handler service after Successful DC Refund Txn with ONUS merchant")
    public void PGP_28980_verifyNotificationQueueHandleraferRefungDCtxnONUSMerchant(@Optional("enhancedweb") String theme) throws PGPException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry_PG2_Refund, theme).setTXN_AMOUNT("10").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .assertAll();
        }
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"RefundSuccessPromoServiceImpl.pushPayloadInKafka()\" | grep \"REFUND\"";
        String merchantstatusfacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
        Assertions.assertThat(merchantstatusfacadelogs).contains("onPaytm='true'");

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify OnPaytm=false in promo payload in queue handler service after Successful DC Refund Txn with OFFUS merchant")
    public void PGP_28980_verifyNotificationQueueHandleraferRefungDCtxnOFFUSMerchant(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PG2_Refund, theme).setTXN_AMOUNT("10").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        Test:
        {
            initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
            getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateStatus(TXN_SUCCESS, 0)
                    .validateRespCode("10",0)
                    .validateRESPMSG("Refund Successfull",0)
                    .validateRRNCodeIsNotNull(0)
                    .validateBANKTXNIDIsNotNull(0)
                    .validateREFUNDIDIsNotNull(0)
                    .validatePAYMENTMODE("DC",0)
                    .validateSuccessRefund()
                    .assertAll();
        }
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"RefundSuccessPromoServiceImpl.pushPayloadInKafka()\" | grep \"REFUND\"";
        String merchantstatusfacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
        Assertions.assertThat(merchantstatusfacadelogs).contains("onPaytm='false'");

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();

    }
    @Owner(Constants.Owner.AJEESH)
    @Feature("PPSL-888")
    @Parameters({"theme"})
    @Test(description = "Verify that when key is not found in Redis , call to merchant center is done and logger is printed")
    public void successfulRefundresultCodeMC(@Optional("enhancedweb") String theme) throws PGPException {
        String expectedLoggers = "response codes not found in redis, calling merchant center for responseCodeId";
        prerequisite:
        {
            validateRefundAllowedWithChecksum(MerchantType.PGOnly_PG2_Refund.getId());
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


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), postConvFlag);
        getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.REFUND_SERVICE_LOGS,orderDTO.getORDER_ID(),"getResultInfoFromMerchantCenter");
        Assertions.assertThat(logs).contains(expectedLoggers);
    }

    

}
