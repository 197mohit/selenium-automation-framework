package scripts.merchantStatus;

import com.paytm.api.*;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Body;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.HandlerInternalTxnstatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Head;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.util.Date;

public class AppInvokeMerchantStatusTests extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41419")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a successful txn.")
    public void txnStatus(@Optional("enhancedwap_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if (cashierPage.ErrorRetryButton().isElementPresent()) {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41419")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a pending txn.")
    public void txnStatusInit(@Optional("enhancedwap_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("99.84")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if (cashierPage.ErrorRetryButton().isElementPresent()) {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41419")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a failed txn.")
    public void txnStatusClosed(@Optional("enhancedweb_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if (cashierPage.ErrorRetryButton().isElementPresent()) {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("750")
                .validateRespMsg("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank")
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41419")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for invalid txn. details.")
    public void txnStatusInvalid(@Optional("enhancedweb_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if (cashierPage.ErrorRetryButton().isElementPresent()) {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID() + "invalid");
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID() + "invalid")
                .validateTxnAmount(Constants.ValidationType.EMPTY)
                .validateStatus("TXN_FAILURE")
                .validateTxnType("")
                .validateGatewayName("")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("")
                .validateRefundAmnt(Constants.ValidationType.EMPTY)
                .validateTxnDate(Constants.ValidationType.EMPTY)
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42086")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a successful txn.")
    public void txnStatus_03(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getKey())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);

        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.version")).isEqualToIgnoringCase("v1");
        softly.assertThat(jsonPath.getString("head.clientId")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.signature")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("01");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        softly.assertThat(jsonPath.getString("body.txnId")).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(jsonPath.getString("body.bankTxnId")).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_TXN_ID));
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("20.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(jsonPath.getString("body.bankName")).isEqualToIgnoringCase(Constants.Bank.ICICINB.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.cardScheme")).isEqualToIgnoringCase("VISA");
        softly.assertThat(jsonPath.getString("body.rrnCode")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.authCode")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42086")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a pending txn.")
    public void txnStatusInit_03(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("77")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING")
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getKey())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);

        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.version")).isEqualToIgnoringCase("v1");
        softly.assertThat(jsonPath.getString("head.clientId")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.signature")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("PENDING");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("402");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("We are processing your transaction.");
        softly.assertThat(jsonPath.getString("body.txnId")).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("77.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42086")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a failed txn.")
    public void txnStatusClosed_03(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("2.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getKey())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);

        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.version")).isEqualToIgnoringCase("v1");
        softly.assertThat(jsonPath.getString("head.clientId")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.signature")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("100233");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank");
        softly.assertThat(jsonPath.getString("body.txnId")).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("2.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(jsonPath.getString("body.bankName")).isEqualToIgnoringCase(Constants.Bank.HDFCBANK.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.cardScheme")).isEqualToIgnoringCase("VISA");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42086")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for invalid txn. details.")
    public void txnStatusInvalid_03(@Optional("enhancedwap_revamp")String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID() + "invalid", orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey())
                .build();

        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.version")).isEqualToIgnoringCase("v1");
        softly.assertThat(jsonPath.getString("head.clientId")).isNotEmpty();
        softly.assertThat(jsonPath.getString("head.signature")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("334");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid Order Id.");
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID()+"invalid");
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a successful txn.")
    public void txnStatus_02(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a pending txn.")
    public void txnStatusInit_02(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("99.84")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateStatus("PENDING")
                .assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatusPending(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a failed txn.")
    public void txnStatusClosed_02(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setTxnValue("99.99")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AddnPay,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_FAILURE")
                .assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for invalid txn. details.")
    public void txnStatusInvalid_02(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID() + "invalid");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("head.clientId").toString()).isEqualToIgnoringCase("upi-app");
        softly.assertThat(response.jsonPath().get("head.version").toString()).isEqualToIgnoringCase("V1");
        softly.assertThat(response.jsonPath().get("head.tokenType").toString()).isEqualToIgnoringCase("JWT");
        softly.assertThat(response.jsonPath().get("head.token").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("head.responseTimestamp").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.txnId").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.bankTxnId").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.orderId").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)+"invalid");
        softly.assertThat(response.jsonPath().get("body.txnAmount").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.txnStatus").toString()).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().get("body.txnType").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.gatewayName").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.txnResponseCode").toString()).isEqualToIgnoringCase("334");
        softly.assertThat(response.jsonPath().get("body.txnResponseMsg").toString()).isEqualToIgnoringCase("Invalid Order Id.");
        softly.assertThat(response.jsonPath().get("body.mid").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID));
        softly.assertThat(response.jsonPath().get("body.paymentMode").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.refundAmt").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.txnDate").toString()).isEqualToIgnoringCase("");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCodeId").toString()).isEqualToIgnoringCase("0000");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41417")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a successful txn.")
    public void txnStatus_01(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.BANKTXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.STATUS").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNTYPE").toString()).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.GATEWAYNAME").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.RESP_CODE)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[Txn Successful.]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.BANKNAME").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.MID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.PAYMENTMODE").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.PAYMENT_MODE)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDAMT").toString()).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNDATE").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDID").toString()).isEqualToIgnoringCase("[]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41417")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a pending txn.")
    public void txnStatusInit_01(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("77")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING")
                .assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.STATUS").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNTYPE").toString()).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.GATEWAYNAME").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[400]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[We are waiting for the response from bank.]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.BANKNAME").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.MID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.PAYMENTMODE").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.PAYMENT_MODE)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDAMT").toString()).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNDATE").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDID").toString()).isEqualToIgnoringCase("[]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41417")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a failed txn.")
    public void txnStatusClosed_01(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("77")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .assertAll();


        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.BANKTXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.STATUS").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNTYPE").toString()).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.GATEWAYNAME").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[810]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[ORDER IS CLOSE.]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.BANKNAME").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.MID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.PAYMENTMODE").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.PAYMENT_MODE)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDAMT").toString()).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNDATE").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDID").toString()).isEqualToIgnoringCase("[]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41417")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for invalid txn. details.")
    public void txnStatusInvalid_01(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY,initTxnDTO.getBody().getOrderId(),txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID() + "invalid", "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[331]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42062")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a successful txn.")
    public void txnStatus_04(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("20.00");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("01");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Txn Success");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEqualToIgnoringCase(Constants.Bank.ICICINB.toString());
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BIN")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42062")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a pending txn.")
    public void txnStatusInit_04(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("77")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("77.00");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("PENDING");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("402");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42062")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a failed txn.")
    public void txnStatusClosed_04(@Optional("enhancedwap_revamp")String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("99.99")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("99.99");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("100233");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEqualToIgnoringCase(Constants.Bank.HDFCBANK.toString());
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42062")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for invalid txn. details.")
    public void txnStatusInvalid_04(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID()+"invalid");
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID()+"invalid");
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEmpty();
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("334");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Invalid Order Id.");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a successful txn.")
    public void txnStatus_05(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+orderDTO.getORDER_ID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[20.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_SUCCESS]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[01]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[Txn Successful.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+orderDTO.getMID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a pending txn.")
    public void txnStatusInit_05(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("77")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING")
                .assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+orderDTO.getORDER_ID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[77.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[PENDING]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[400]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[We are waiting for the response from bank.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+orderDTO.getMID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a failed txn.")
    public void txnStatusClosed_05(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("99.99")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+orderDTO.getORDER_ID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[99.99]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_FAILURE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[810]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[ORDER IS CLOSE.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+orderDTO.getMID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for invalid txn. details.")
    public void txnStatusInvalid_05(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID()+"invalid");
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[331]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }
}