package scripts.merchantStatus;

import com.paytm.api.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Body;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.HandlerInternalTxnstatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Head;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;


import java.io.IOException;
import java.util.Date;


public class CheckoutJSmerchantStatusTests extends CheckoutJsBase {

    public static void validateOrderStatus(String mid, String orderID, ResponsePage responsePage) {
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(mid, orderID);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("head.clientId").toString()).isEqualToIgnoringCase("upi-app");
        softly.assertThat(response.jsonPath().get("head.version").toString()).isEqualToIgnoringCase("V1");
        softly.assertThat(response.jsonPath().get("head.tokenType").toString()).isEqualToIgnoringCase("JWT");
        softly.assertThat(response.jsonPath().get("head.token").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("head.responseTimestamp").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.txnId").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(response.jsonPath().get("body.bankTxnId").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_TXN_ID));
        softly.assertThat(response.jsonPath().get("body.orderId").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID));
        softly.assertThat(response.jsonPath().get("body.txnAmount").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT));
        softly.assertThat(response.jsonPath().get("body.txnStatus").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS));
        softly.assertThat(response.jsonPath().get("body.txnType").toString()).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().get("body.gatewayName").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.txnResponseCode").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.RESP_CODE));
        softly.assertThat(response.jsonPath().get("body.txnResponseMsg").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.RESP_MSG));
        softly.assertThat(response.jsonPath().get("body.mid").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID));
        softly.assertThat(response.jsonPath().get("body.paymentMode").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.PAYMENT_MODE));
        softly.assertThat(response.jsonPath().get("body.refundAmt").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.txnDate").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCodeId").toString()).isEqualToIgnoringCase("0000");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    public static void validateOrderStatusPending(String mid, String orderID, ResponsePage responsePage) {
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(mid, orderID);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("head.clientId").toString()).isEqualToIgnoringCase("upi-app");
        softly.assertThat(response.jsonPath().get("head.version").toString()).isEqualToIgnoringCase("V1");
        softly.assertThat(response.jsonPath().get("head.tokenType").toString()).isEqualToIgnoringCase("JWT");
        softly.assertThat(response.jsonPath().get("head.token").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("head.responseTimestamp").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.txnId").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(response.jsonPath().getString("body.bankTxnId")).isNotNull();
        softly.assertThat(response.jsonPath().getString("body.bankTxnId")).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.orderId").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID));
        softly.assertThat(response.jsonPath().get("body.txnAmount").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT));
        softly.assertThat(response.jsonPath().get("body.txnStatus").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS));
        softly.assertThat(response.jsonPath().get("body.txnType").toString()).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().get("body.txnResponseCode").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.RESP_CODE));
        softly.assertThat(response.jsonPath().get("body.txnResponseMsg").toString()).isEqualToIgnoringCase(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        softly.assertThat(response.jsonPath().get("body.mid").toString()).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID));
        softly.assertThat(response.jsonPath().get("body.refundAmt").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.txnDate").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCodeId").toString()).isEqualToIgnoringCase("0000");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnStatus for a successful txn using Checkout js flow")
    public void txnStatus(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnStatus for a pending txn using Checkout js flow")
    public void txnStatusInit(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnStatus for a failed txn using Checkout js flow")
    public void txnStatusClosed(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("100233")
                .validateRespMsg("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank")
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnStatus for invalid txn details using Checkout js flow")
    public void txnStatusInvalid(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId()+"invalid");
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId()+"invalid")
                .validateTxnAmount(Constants.ValidationType.EMPTY)
                .validateStatus("TXN_FAILURE")
                .validateTxnType("")
                .validateGatewayName("")
                .validateRespCode("334")
                .validateRespMsg("Invalid Order Id.")
                .validateBankName("")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("")
                .validateRefundAmnt(Constants.ValidationType.EMPTY)
                .validateTxnDate(Constants.ValidationType.EMPTY)
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnListStatus for a successful txn using Checkout js flow")
    public void
    txnStatus_01(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey(), initTxnDTO.getBody().getOrderId(), "ALL");
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
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnListStatus for a pending txn using Checkout js flow")
    public void txnStatusInit_01(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey(), initTxnDTO.getBody().getOrderId(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.STATUS").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNTYPE").toString()).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.GATEWAYNAME").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[400]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[We are waiting for the response from bank.]");
        //softly.assertThat(response.jsonPath().get("TXN_LIST.BANKNAME").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.MID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.MID)+"]");
        //softly.assertThat(response.jsonPath().get("TXN_LIST.PAYMENTMODE").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.PAYMENT_MODE)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDAMT").toString()).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNDATE").toString()).isNotEmpty();
        softly.assertThat(response.jsonPath().get("TXN_LIST.REFUNDID").toString()).isEqualToIgnoringCase("[]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/getTxnListStatus for a failed txn using Checkout js flow")
    public void txnStatusClosed_01(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey(), initTxnDTO.getBody().getOrderId(), "ALL");
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
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To Verify response of  /merchant-status/getTxnListStatus for invalid txn details using Checkout js flow")
    public void txnStatusInvalid_01(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey(), initTxnDTO.getBody().getOrderId()+"invalid", "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[331]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/v2/order/status for a successful txn using Checkout js flow")
    public void txnStatus_02(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        validateOrderStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/v2/order/status for a pending txn using Checkout js flow")
    public void txnStatusInit_02(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        validateOrderStatusPending(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/v2/order/status for a failed txn using Checkout js flow")
    public void txnStatusClosed_02(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

       validateOrderStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To Verify response of /merchant-status/v2/order/status for invalid txn details using Checkout js flow")
    public void txnStatusInvalid_02(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId()+"invalid");
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
        softly.assertAll();

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/api/v1/getPaymentStatus for a successful txn using Checkout js flow")
    public void txnStatus_04(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey())
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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(jsonPath.getString("body.bankName")).isEqualToIgnoringCase(Constants.Bank.ICICINB.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.cardScheme")).isEqualToIgnoringCase("VISA");
        softly.assertThat(jsonPath.getString("body.rrnCode")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.authCode")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/api/v1/getPaymentStatus for a pending txn using Checkout js flow")
    public void txnStatusInit_04(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey())
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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("77.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/api/v1/getPaymentStatus for a failed txn using Checkout js flow")
    public void txnStatusClosed_04(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey())
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
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("750");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank");
        softly.assertThat(jsonPath.getString("body.txnId")).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("99.99");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(jsonPath.getString("body.bankName")).isEqualToIgnoringCase(Constants.Bank.HDFCBANK.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.cardScheme")).isEqualToIgnoringCase("VISA");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/api/v1/getPaymentStatus for invalid txn details using Checkout js flow")
    public void txnStatusInvalid_04(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId()+"invalid", initTxnDTO.getBody().getMid(), Constants.MerchantType.NATIVE_HYBRID.getKey())
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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId()+"invalid");
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a successful txn using Checkout js flow")
    public void txnStatus_05(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("1.00");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("01");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Txn Success");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEqualToIgnoringCase(Constants.Bank.ICICINB.toString());
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a pending txn using Checkout js flow")
    public void txnStatusInit_05(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("77.00");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("PENDING");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("402");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("We are processing your transaction.");
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a failed txn using Checkout js flow")
    public void txnStatusClosed_05(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("99.99");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("100233");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEqualToIgnoringCase(Constants.Bank.ICICINB.toString());
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertAll();

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for invalid txn details using Checkout js flow")
    public void txnStatusInvalid_05(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId() + "invalid");
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId()+"invalid");
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEmpty();
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("334");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Invalid Order Id.");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isEmpty();
        softly.assertAll();

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a successful txn using Checkout js flow")
    public void txnStatus_03(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+initTxnDTO.getBody().getOrderId()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[1.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_SUCCESS]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.GATEWAYNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[01]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[Txn Successful.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isEqualToIgnoringCase("[ICICI Bank]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+initTxnDTO.getBody().getMid()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isEqualToIgnoringCase("[CC]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a pending txn using Checkout js flow")
    public void txnStatusInit_03(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+initTxnDTO.getBody().getOrderId()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[77.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[PENDING]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[400]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[We are waiting for the response from bank.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isEqualToIgnoringCase("[]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+initTxnDTO.getBody().getMid()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isEqualToIgnoringCase("[]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a failed txn using Checkout js flow")
    public void txnStatusClosed_03(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.98")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+initTxnDTO.getBody().getOrderId()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[99.98]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_FAILURE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[810]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[ORDER IS CLOSE.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isEqualToIgnoringCase("[ICICI Bank]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+initTxnDTO.getBody().getMid()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isEqualToIgnoringCase("[CC]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "Verfiy response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for invalid txn details using Checkout js flow")
    public void txnStatusInvalid_03(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId() + "invalid");
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[331]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40227")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS/APP for a successful txn.")
    public void txnStatus_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        HandlerInternalTxnstatusDTO handlerInternalTxnstatusDTO = new HandlerInternalTxnstatusDTO();
        handlerInternalTxnstatusDTO.setBody(new Body(initTxnDTO.getBody().getOrderId())).setHead(new Head(Constants.MerchantType.NATIVE_HYBRID.getId(), user.ssoToken()));
        HandlerTxnStatusApi handlerTxnStatusApi = new HandlerTxnStatusApi(handlerInternalTxnstatusDTO);
        Response response= handlerTxnStatusApi.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("head.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("head.version")).isEqualToIgnoringCase("v2");
        softly.assertThat(response.jsonPath().getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.requestId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.clientId")).isEqualToIgnoringCase("paytm-pg-client-staging");
        softly.assertThat(response.jsonPath().getString("body.signature")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(response.jsonPath().getString("body.TXNAMOUNT")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.STATUS")).isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("body.TXNTYPE")).isEqualToIgnoringCase("SALE");;
        softly.assertThat(response.jsonPath().getString("body.GATEWAYNAME")).isEqualToIgnoringCase("HDFC");;
        softly.assertThat(response.jsonPath().getString("body.RESPCODE")).isEqualTo("01");
        softly.assertThat(response.jsonPath().getString("body.RESPMSG")).isEqualToIgnoringCase("Txn Success");
        softly.assertThat(response.jsonPath().getString("body.BANKNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.MID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.PAYMENTMODE")).isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().getString("body.REFUNDAMT")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.extendInfo.key")).isEqualToIgnoringCase("value");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCodeId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40227")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS/APP for a pending txn.")
    public void txnStatusInit_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        HandlerInternalTxnstatusDTO handlerInternalTxnstatusDTO = new HandlerInternalTxnstatusDTO();
        handlerInternalTxnstatusDTO.setBody(new Body(initTxnDTO.getBody().getOrderId())).setHead(new Head(Constants.MerchantType.NATIVE_HYBRID.getId(), user.ssoToken()));
        HandlerTxnStatusApi handlerTxnStatusApi = new HandlerTxnStatusApi(handlerInternalTxnstatusDTO);
        Response response = handlerTxnStatusApi.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("head.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("head.version")).isEqualToIgnoringCase("v2");
        softly.assertThat(response.jsonPath().getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.requestId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.clientId")).isEqualToIgnoringCase("paytm-pg-client-staging");
        softly.assertThat(response.jsonPath().getString("body.signature")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(response.jsonPath().getString("body.TXNAMOUNT")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.STATUS")).isEqualToIgnoringCase("PENDING");
        softly.assertThat(response.jsonPath().getString("body.TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("body.RESPCODE")).isEqualTo("402");
        softly.assertThat(response.jsonPath().getString("body.RESPMSG")).isEqualToIgnoringCase("We are processing your transaction.");
        softly.assertThat(response.jsonPath().getString("body.MID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCodeId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40227")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS/APP for a failed txn.")
    public void txnStatusClosed_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        HandlerInternalTxnstatusDTO handlerInternalTxnstatusDTO = new HandlerInternalTxnstatusDTO();
        handlerInternalTxnstatusDTO.setBody(new Body(initTxnDTO.getBody().getOrderId())).setHead(new Head(Constants.MerchantType.NATIVE_HYBRID.getId(), user.ssoToken()));
        HandlerTxnStatusApi handlerTxnStatusApi = new HandlerTxnStatusApi(handlerInternalTxnstatusDTO);
        Response response = handlerTxnStatusApi.executeUntilExpectedConditionMet("body.STATUS","TXN_FAILURE",3,12);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("head.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("head.version")).isEqualToIgnoringCase("v2");
        softly.assertThat(response.jsonPath().getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.requestId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.clientId")).isEqualToIgnoringCase("paytm-pg-client-staging");
        softly.assertThat(response.jsonPath().getString("body.signature")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        softly.assertThat(response.jsonPath().getString("body.TXNAMOUNT")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("body.TXNTYPE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.GATEWAYNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.RESPCODE")).isEqualTo("100233");
        softly.assertThat(response.jsonPath().getString("body.RESPMSG")).isEqualToIgnoringCase("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank");
        softly.assertThat(response.jsonPath().getString("body.BANKNAME")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.MID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.PAYMENTMODE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.REFUNDAMT")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.extendInfo.key")).isEqualToIgnoringCase("value");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCodeId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40227")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS/APP for invalid txn. details")
    public void txnStatusInvalid_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        HandlerInternalTxnstatusDTO handlerInternalTxnstatusDTO = new HandlerInternalTxnstatusDTO();
        handlerInternalTxnstatusDTO.setBody(new Body(initTxnDTO.getBody().getOrderId()+"invalid")).setHead(new Head(Constants.MerchantType.NATIVE_HYBRID.getId(), user.ssoToken()));
        HandlerTxnStatusApi handlerTxnStatusApi = new HandlerTxnStatusApi(handlerInternalTxnstatusDTO);
        Response response = handlerTxnStatusApi.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("head.mid")).isEqualToIgnoringCase(initTxnDTO.getBody().getMid());
        softly.assertThat(response.jsonPath().getString("head.version")).isEqualToIgnoringCase("v2");
        softly.assertThat(response.jsonPath().getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.requestId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("head.clientId")).isEqualToIgnoringCase("paytm-pg-client-staging");
        softly.assertThat(response.jsonPath().getString("body.signature")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.ORDERID")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId()+"invalid");
        softly.assertThat(response.jsonPath().getString("body.TXNAMOUNT")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.STATUS")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(response.jsonPath().getString("body.TXNTYPE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.GATEWAYNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.RESPCODE")).isEqualTo("334");
        softly.assertThat(response.jsonPath().getString("body.RESPMSG")).isEqualToIgnoringCase("Invalid Order Id.");
        softly.assertThat(response.jsonPath().getString("body.BANKNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.MID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.PAYMENTMODE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.REFUNDAMT")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.TXNDATE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("body.extendInfo.key")).isEqualToIgnoringCase("value");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCodeId")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("Success");
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-49001")
    @Parameters({"theme"})
    @Test(description = "To Verify checkoutJs transaction and merchant status response")
    public void PGP_49001_TC04(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DC_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO payemntdto=new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC,payemntdto);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for Native_MF TPV Transaction via CheckoutJs Flow")
    public void PAPR_4739_TC_MF04(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setAggrMid(Constants.MerchantType.MUTUAL_FUND_AGGR.getId())
                .setRequestType("NATIVE_MF")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("7777777777")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on is not present on Cashier Page UI for Native_MF Non-TPV Transaction via CheckoutJs Flow")
    public void PAPR_4739_TC_MF06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setAggrMid(Constants.MerchantType.MUTUAL_FUND_AGGR.getId())
                .setRequestType("NATIVE_MF")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tpvAccountAlert().assertNotVisible();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for Stock Trade TPV Transaction via CheckoutJs Flow")
    public void PAPR_4739_TC_ST04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.STOCK_TRADE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE_ST")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("7777777777")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert is not visible on Cashier Page UI for Stock Trade Non-TPV Transaction via CheckoutJs Flow")
    public void PAPR_4739_TC_ST06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.STOCK_TRADE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE_ST")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tpvAccountAlert().assertNotVisible();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert is visible on Cashier Page UI for MF SIP TPV Transaction via CheckoutJs Flow")
    public void PAPR_4739_TC_SIP04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_Pg2_MID2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("7777777777")
                .setTxnValue("25000")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert is not visible on Cashier Page UI for MF SIP Non-TPV Transaction via CheckoutJs Flow")
    public void PAPR_4739_TC_SIP06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_Pg2_MID2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("25000")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tpvAccountAlert().assertNotVisible();
    }
}