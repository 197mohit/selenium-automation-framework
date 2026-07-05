package scripts.merchantStatus;

import com.paytm.api.*;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.QRHelper;
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
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

public class OfflineMerchantStatusTests extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();
    String paymentMode = "CREDIT_CARD";
    String txnAmount = "2.00";

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41419")
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a successful txn.")
    public void txnStatus() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
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
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a pending txn.")
    public void txnStatusInit() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, "77.00", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("PENDING").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
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
    @Test(description = "To verify response of /merchant-status/getTxnStatus for invalid txn. details.")
    public void txnStatusInvalid() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a successful txn.")
    public void txnStatus_03() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey())
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
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("2.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(jsonPath.getString("body.bankName")).isEqualToIgnoringCase(Constants.Bank.HDFCBANK.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.00");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.cardScheme")).isEqualToIgnoringCase("VISA");
        softly.assertThat(jsonPath.getString("body.rrnCode")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.authCode")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42086")
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a pending txn.")
    public void txnStatusInit_03() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, "77", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING")
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey())
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
        softly.assertThat(jsonPath.getString("body.bankTxnId")).isNotEmpty();
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
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for invalid txn. details.")
    public void txnStatusInvalid_03() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("810");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Payment failed due to a technical error. Please try after some time.");
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID()+"invalid");
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Test(description = "To verify response of /merchant-status/v2/order/status for a successful txn.")
    public void txnStatus_02() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Test(description = "To verify response of /merchant-status/v2/order/status for a pending txn.")
    public void txnStatusInit_02() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, "99.84", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateStatus("PENDING").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatusPending(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-41418")
    @Test(description = "To verify response of /merchant-status/v2/order/status for invalid txn. details.")
    public void txnStatusInvalid_02() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateStatus("TXN_SUCCESS").assertAll();

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
        softly.assertThat(response.jsonPath().get("body.txnResponseCode").toString()).isEqualToIgnoringCase("810");
        softly.assertThat(response.jsonPath().get("body.txnResponseMsg").toString()).isEqualToIgnoringCase("Payment failed due to a technical error. Please try after some time.");
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
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a successful txn.")
    public void txnStatus_01() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a pending txn.")
    public void txnStatusInit_01() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, "99.84", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for invalid txn. details.")
    public void txnStatusInvalid_01() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.AddnPay, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a successful txn.")
    public void txnStatus_04() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("2.00");
        softly.assertThat(response.jsonPath().getString("STATUS")).isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("TXNTYPE")).isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().getString("GATEWAYNAME")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("01");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Txn Success");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEqualToIgnoringCase(Constants.Bank.HDFCBANK.toString());
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEqualToIgnoringCase("CC");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.00");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BIN")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42062")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a pending txn.")
    public void txnStatusInit_04() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, "77", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("PENDING").assertAll();

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
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("We are processing your transaction.");
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42062")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for invalid txn. details.")
    public void txnStatusInvalid_04() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
        softly.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("810");
        softly.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("Payment failed due to a technical error. Please try after some time.");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).isEmpty();
        softly.assertThat(response.jsonPath().getString("MID")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).isEmpty();
        softly.assertThat(response.jsonPath().getString("TXNDATE")).isEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a successful txn.")
    public void txnStatus_05() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[2.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_SUCCESS]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[01]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[Txn Successful.]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isEqualToIgnoringCase("[HDFC Bank]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+orderDTO.getMID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isEqualToIgnoringCase("[CC]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a pending txn.")
    public void txnStatusInit_05() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, "77", user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKNAME")).isEqualToIgnoringCase("[HDFC Bank]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.MID")).isEqualToIgnoringCase("["+orderDTO.getMID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.PAYMENTMODE")).isEqualToIgnoringCase("[CC]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDAMT")).isEqualToIgnoringCase("[0.0]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNDATE")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.REFUNDID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.LASTFOURDIGITS")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-42087")
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for invalid txn. details.")
    public void txnStatusInvalid_05() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, txnAmount, user);
        String qrCodeId = orderDTO.getTxnId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode(paymentMode)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
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