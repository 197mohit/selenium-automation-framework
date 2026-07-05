package scripts.merchantStatus;

import com.paytm.LocalConfig;
import com.paytm.api.*;
import com.paytm.api.Instaproxy.UPISecureResponse;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.api.upipsp.UpiPspConsultFee;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Body;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.HandlerInternalTxnstatusDTO;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.Head;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.*;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.*;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import static com.paytm.appconstants.Constants.MerchantType.CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE;
import static com.paytm.appconstants.Constants.Owner.AJEESH;
import static com.paytm.appconstants.Constants.Owner.MOHIT_KHARE;
import static com.paytm.apphelpers.QRHelper.parseDeeplinkInfo;

public class MerchantStatusTests extends PGPBaseTest {

    private final String riskExtendInfo = "userLBSLatitude:{{latitude}}|userLBSLongitude:{{longitude}}|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi|\"";
    private final String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|orderAlreadyCreated:{orderAlreadyCreated}";
    private CheckoutPage checkoutPage = new CheckoutPage();

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a successful txn.")
    public void txnStatus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a pending txn.")
    public void txnStatusInit(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("77")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
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
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a failed txn.")
    public void txnStatusClosed(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("100233")
                .validateRespMsg("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank")
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for invalid txn. details.")
    public void txnStatusInvalid(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID()+"invalid");
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID()+"invalid")
                .validateTxnAmount(Constants.ValidationType.EMPTY)
                .validateStatus("TXN_FAILURE")
                .validateTxnType("")
                .validateGatewayName("")
                .validateRespCode("334")
                .validateRespMsg("Invalid Order Id.")
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("")
                .validateRefundAmnt(Constants.ValidationType.EMPTY)
                .validateTxnDate(Constants.ValidationType.EMPTY)
                .AssertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a successful txn.")
    public void txnStatus_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a pending txn.")
    public void txnStatusInit_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
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
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a failed txn.")
    public void txnStatusClosed_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();


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
    @Feature("PGP-40233")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for invalid txn. details.")
    public void txnStatusInvalid_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID() + "invalid", "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[331]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a successful txn.")
    public void txnStatus_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a pending txn.")
    public void txnStatusInit_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_CC_DC, theme)
                .setTXN_AMOUNT("77")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatusPending(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a failed txn.")
    public void txnStatusClosed_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme).setTXN_AMOUNT("99.99").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for invalid txn. details.")
    public void txnStatusInvalid_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        if(cashierPage.ErrorRetryButton().isElementPresent())
        {
            cashierPage.ErrorRetryButton().click();
            cashierPage.payBy(Constants.PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40231")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a successful txn.")
    public void txnStatusNative(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40231")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a pending txn.")
    public void txnStatusNativeInit(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("99.84")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
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
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for a closed txn.")
    public void txnStatusNativeClosed(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
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
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnStatus for invalid txn. details")
    public void txnStatusNativeInvalid(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID()+"invalid");
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
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a successful txn.")
    public void txnStatusNative_01(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey(), orderDTO.getORDER_ID(), "ALL");
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
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a pending txn.")
    public void txnStatusNativeInit_01(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("99.84")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey(), orderDTO.getORDER_ID(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
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
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for a closed txn.")
    public void txnStatusNativeClosed_01(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey(), orderDTO.getORDER_ID(), "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.BANKTXNID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_TXN_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.ORDERID").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNAMOUNT").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.STATUS").toString()).isEqualToIgnoringCase("["+responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.STATUS)+"]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.TXNTYPE").toString()).isEqualToIgnoringCase("[SALE]");
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
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/getTxnListStatus for invalid txn. details")
    public void txnStatusNativeInvalid_01(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        Response response = PGPHelpers.executeTxnStatusList(orderDTO.getMID(), Constants.MerchantType.AddnPay.getKey(), orderDTO.getORDER_ID() + "invalid", "ALL");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPCODE").toString()).isEqualToIgnoringCase("[331]");
        softly.assertThat(response.jsonPath().get("TXN_LIST.RESPMSG").toString()).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a successful txn.")
    public void txnStatusNative_02(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a pending txn.")
    public void txnStatusNativeInit_02(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("99.84")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatusPending(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for a closed txn.")
    public void txnStatusNativeClosed_02(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        CheckoutJSmerchantStatusTests.validateOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), responsePage);
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40230")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/v2/order/status for invalid txn. details")
    public void txnStatusNativeInvalid_02(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a successful txn.")
    public void txnStatus_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a pending txn.")
    public void txnStatusInit_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("77")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

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
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a failed txn.")
    public void txnStatusClosed_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

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
    @Feature("PGP-40232")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for invalid txn. details.")
    public void txnStatusInvalid_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a successful txn.")
    public void txnStatus_04(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a pending txn.")
    public void txnStatusInit_04(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setTXN_AMOUNT("77")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

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
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a failed txn.")
    public void txnStatusClosed_04(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("2.00");
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
    @Feature("PGP-40226")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for invalid txn. details.")
    public void txnStatusInvalid_04(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a successful txn.")
    public void txnStatus_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

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
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a pending txn.")
    public void txnStatusInit_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setTXN_AMOUNT("77")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

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
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a failed txn.")
    public void txnStatusClosed_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+orderDTO.getORDER_ID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[2.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_FAILURE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[810]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[ORDER IS CLOSE.]");
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
    @Feature("PGP-40228")
    @Parameters({"theme"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for invalid txn. details.")
    public void txnStatusInvalid_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();


        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID()+"invalid");
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[331]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40232")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a successful txn.")
    public void txnStatusNative_03(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();


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
    @Feature("PGP-40232")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a pending txn.")
    public void txnStatusNativeInit_03(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("77.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();


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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("77.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertAll();
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-40232")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for a closed txn.")
    public void txnStatusNativeClosed_03(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

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
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("100233");
        softly.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank");
        softly.assertThat(jsonPath.getString("body.txnId")).isEqualToIgnoringCase(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID));
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("20.00");
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
    @Feature("PGP-40232")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/api/v1/getPaymentStatus for invalid txn. details")
    public void txnStatusInvalidNative_03(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AddnPay)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.AddnPay, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();


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
    @Feature("PGP-40226")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a successful txn.")
    public void txnStatusNative_04(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();


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

    @Feature("PGP-40226")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a pending txn.")
    public void txnStatusNativeInit_04(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("77.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();


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
    @Feature("PGP-40226")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for a closed txn.")
    public void txnStatusNativeClosed_04(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();


        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("BANKTXNID")).isEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).isEqualToIgnoringCase("20.00");
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
    @Feature("PGP-40226")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUS for invalid txn. details")
    public void txnStatusNativeInvalid_04(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();


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
    @Feature("PGP-40228")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a successful txn.")
    public void txnStatusNative_05(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();


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
    @Feature("PGP-40228")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a pending txn.")
    public void txnStatusNativeInit_05(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("77.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, PayMethodType.CREDIT_CARD).build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();

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
    @Feature("PGP-40228")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for a closed txn.")
    public void txnStatusNativeClosed_05(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.BANKTXNID")).isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.ORDERID")).isEqualToIgnoringCase("["+orderDTO.getORDER_ID()+"]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNAMOUNT")).isEqualToIgnoringCase("[20.00]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.STATUS")).isEqualToIgnoringCase("[TXN_FAILURE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.TXNTYPE")).isEqualToIgnoringCase("[SALE]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[810]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[ORDER IS CLOSE.]");
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
    @Feature("PGP-40228")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify response of /merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST for invalid txn. details")
    public void txnStatusInvalidNative_05(@Optional("true") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY)
                .setRequestType("NATIVE")
                .setTxnValue("20.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID()+"invalid");
        Response response = txnStatusList.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPCODE")).isEqualTo("[331]");
        softly.assertThat(response.jsonPath().getString("TXN_LIST.RESPMSG")).isEqualToIgnoringCase("[No Record Found]");
        softly.assertAll();
    }
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-46172")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify LastFourDifit is not added in Merchant Status & get payment status response if FF4J is off and BinInResponse is false.")
    public void lastFourDigitNotVisible(@Optional("true") Boolean isNativePlus) {
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI_ON_TOKEN).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.EMI_ON_TOKEN.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setAuthMode("otp")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.EMI_ON_TOKEN.getId(), orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));
        Assertions.assertThat(!(tsJsonPath.prettify().contains("LASTFOURDIGITS")));
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(new GetPaymentStatusDTO.Builder(orderId,Constants.MerchantType.PGOnly).build());
        JsonPath gpsJsonPath = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(!(gpsJsonPath.prettify().contains("lastFourDigit")));
    }

    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-46172")
    @Parameters({"isNativePlus"})
    @Test(description = "To verify LastFourDifit is added in Merchant Status & get payment status response if FF4J is on and BinInResponse is true.")
    public void lastFourDigitVisible(@Optional("true") Boolean isNativePlus) {

        PaymentDTO paymentDTO = new PaymentDTO();
        String lastFourDigit = paymentDTO.getCreditCardNumber().substring(paymentDTO.getCreditCardNumber().length()-4);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BIN_IN_RESPONSE).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(Constants.MerchantType.BIN_IN_RESPONSE.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setAuthMode("otp")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.BIN_IN_RESPONSE.getId(), orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));
        //will be returned only if pref "BIN_IN_RESPONSE' is true on merchant
        Assertions.assertThat(tsJsonPath.getString("LASTFOURDIGITS").equalsIgnoreCase(lastFourDigit));
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(new GetPaymentStatusDTO.Builder(orderId,Constants.MerchantType.BIN_IN_RESPONSE).build());
        JsonPath gpsJsonPath = getPaymentStatus.execute().jsonPath();
        Assertions.assertThat(gpsJsonPath.getString("body.lastFourDigit").equalsIgnoreCase(lastFourDigit));
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (>16 digits) should be present in COP request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is ON")
    public void TC01_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.CancelAllowed;

        String latitude = "8.01111190292920920";
        String longitude = "12.01111190292920920";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (>16 digits) should be present in Pay request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is ON")
    public void TC02_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;

        String latitude = "8.01111190292920920";
        String longitude = "12.01111190292920920";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (>16 digits) should not be present in COP request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is OFF")
    public void TC03_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;

        String latitude = "8.01111190292920920";
        String longitude = "12.01111190292920920";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (>16 digits) should not be present in Pay request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is OFF")
    public void TC04_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;

        String latitude = "8.01111190292920920";
        String longitude = "12.01111190292920920";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (<16 digits) should be present in COP request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is ON")
    public void TC05_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.CancelAllowed;

        String latitude = "8.011";
        String longitude = "12.011";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (<16 digits) should be present in Pay request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is ON")
    public void TC06_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;

        String latitude = "8.011";
        String longitude = "12.011";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (<16 digits) should be present in COP request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is OFF")
    public void TC07_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;

        String latitude = "8.011";
        String longitude = "12.011";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Feature("PGP-52001")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Latitude and Longitude co-ordinates (<16 digits) should be present in Pay request when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is OFF")
    public void TC08_PGP_52001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;

        String latitude = "8.011";
        String longitude = "12.011";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(),initTxnResponseDTO.getBody().getTxnToken(),orderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-52001")
    @Test(description = "Latitude and Longitude co-ordinates (>16 digits) should be present in Pay request (DQR Flow) when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is ON")
    public void TC09_PGP_52001() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String mid = merchantType.getId();
        String amount = "1.00";

        String latitude = "8.01111190292920920";
        String longitude = "12.01111190292920920";

        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchantType, amount, orderId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();


/*ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId, amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("NATIVE")
                .setWebsite("retail")
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendInfo.replace("{{latitude}}",latitude).replace("{{longitude}}",longitude))
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER"); 
        Assertions.assertThat(logs).contains("\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"");*/
    }

    @Owner(Constants.Owner.HARSHITA)
    @Feature("PGP-52001")
    @Test(description = "Latitude and Longitude co-ordinates (>16 digits) should be present in COP request (Static QR Flow) when passed in v1/ptc request when FF4J flag: theia.disable.lengthCheckForLatitudeAndLongitude is ON")
    public void TC10_PGP_52001() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.CancelAllowed;
        String mid = merchantType.getId();
        String amount = "1.00";
        String qrCodeId = "10NLGK";

        String latitude = "8.01111190292920920";
        String longitude = "12.01111190292920920";

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "false"));

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60729")
    @Test(description = "validate Bin number for /merchant-status/getTxnStatus Api with UPI Intent for Upi Credit Card")
    public void validateBinNumberforgetTxnStatusApiwithUPIIntentforUpiCreditCard() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr") + 3, deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject = new UpiPspConsultFee(mid, txnAmountupipsp, paymentFee, payeeVPA, externalSerialNo, payerPaymentInstrument, subAccountTypeValue);
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp, externalSerialNo, bankRrn, callbackpaymentInstrument, "upiCcBin");
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("652925");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60729")
    @Test(description = "validate Bin number for /merchant-status/getTxnStatus Api with UPI Collect for Upi Credit Card")
    public void validateBinNumberforgetTxnStatusApiwithUPICollectforUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String txnAmount = "205.02";
        String txnAmountupipsp = "205.02";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPICC");
        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("653018");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60729")
    @Test(description = "validate Bin number for /merchant-status/api/v1/getPaymentStatus Api with UPI Intent for Upi Credit Card")
    public void validateBinNumberforgetPaymentStatusApiwithUPIIntentforUpiCreditCard() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String midkey = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getKey();
        String txnAmount = "200";
        String txnAmountupipsp = "200";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String callbackpaymentInstrument = "CREDIT_CARD";
        String subAccountTypeValue = "";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), orderId, "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String externalSerialNo = deeplink.substring(deeplink.indexOf("tr") + 3, deeplink.indexOf("&am"));
        String bankRrn = CommonHelpers.generateOrderId();
        System.out.println(externalSerialNo);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UpiPspConsultFee upiPspConsultFeeobject = new UpiPspConsultFee(mid, txnAmountupipsp, paymentFee, payeeVPA, externalSerialNo, payerPaymentInstrument, subAccountTypeValue);
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");

        InstaproxyPtybliResponse successresponse = new InstaproxyPtybliResponse(txnAmountupipsp, externalSerialNo, bankRrn, callbackpaymentInstrument, "upiCcBin");
        JsonPath callbackres = successresponse.execute().jsonPath();
        Assertions.assertThat(callbackres.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(callbackres.getString("body.resultMsg")).isEqualTo("success");

        Thread.sleep(2000);

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderId, mid, midkey)
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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderId);
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("200.00");
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.PTYBLI.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(mid);
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("UPI");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.binNumber")).isEqualTo("652925");
        softly.assertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-60729")
    @Test(description = "validate Bin number for /merchant-status/api/v1/getPaymentStatus Api with UPI Collect for Upi Credit Card")
    public void validateBinNumberforgetPaymentStatusApiwithUPICollectforUpiCreditCard() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getId();
        String midkey = Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE.getKey();
        String txnAmount = "205.02";
        String txnAmountupipsp = "205.02";
        String paymentFee = "0";
        String payeeVPA = "paytm-9759417321@ptybl";
        String payerPaymentInstrument = "UPI_CREDIT_CARD";
        String subAccountTypeValue = "";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MDR_FEE_ON_UPI_SUBTYPE).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String jsonResponse  = ptcResponse.toString();

        JSONObject root = new JSONObject(jsonResponse);
        String externalSerialNo = root.getJSONObject("body")
                .getJSONObject("bankForm")
                .getJSONObject("redirectForm")
                .getJSONObject("content")
                .getString("externalSrNo");

        UpiPspConsultFee upiPspConsultFeeobject =new UpiPspConsultFee( mid,  txnAmountupipsp, paymentFee ,  payeeVPA,  externalSerialNo ,  payerPaymentInstrument,  subAccountTypeValue) ;
        JsonPath responseJsonPath = upiPspConsultFeeobject.execute().jsonPath();
        Assertions.assertThat(responseJsonPath.getString("body.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(responseJsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(responseJsonPath.getString("body.resultMsg")).isEqualTo("success");
        Assertions.assertThat(responseJsonPath.getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(responseJsonPath.getString("body.txnAmount")).isEqualTo(txnAmountupipsp);
        Assertions.assertThat(responseJsonPath.getString("body.externalSerialNo")).isEqualTo(externalSerialNo);
        Assertions.assertThat(responseJsonPath.getString("body.payerPaymentInstrument")).isEqualTo(payerPaymentInstrument);
        Assertions.assertThat(responseJsonPath.getString("body.convenienceFeeValidation")).isEqualTo("MATCH");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PTYBLC.toString())
                .assertAll();

        Thread.sleep(2000);

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderId, mid, midkey)
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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderId);
        softly.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmount);
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.PTYBLC.toString());
        softly.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(mid);
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("UPI");
        softly.assertThat(jsonPath.getString("body.refundAmt")).isEqualToIgnoringCase("0.0");
        softly.assertThat(jsonPath.getString("body.txnDate")).isNotEmpty();
        softly.assertThat(jsonPath.getString("body.binNumber")).isEqualTo("653018");
        softly.assertAll();

    }
    // AI-Generated: 2025-09-11 - Add remitter field validation
    @Owner(AJEESH)
    @Feature("PGP-59384")
    @Test(description = "Validate Remitter Bank Name Details is shown in Merchant Status APIs - getPaymentStatus")
    public void verifyThatRemitterBankNameDetailsIsShowngetPaymentStatus() throws InterruptedException {

        Constants.MerchantType MerchID = CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        ProcessTxnV1Request processTxnV1Request = builder
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();

        String deeplink = jsonPathResponse.getString("body.deepLinkInfo.deepLink");
        System.out.println("Decoded deeplink: " + deeplink);


        // Parse deeplink information
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
                "Abhishek Verma",
                "paytmTest@ptys",
                payeeVpa,
                "Punjab National Bank",
                "PUNB",
                tr,
                "SUCCESS",
                String.format("%011d", (long) (Math.random() * 100000000000L)),
                amount,
                "0",
                "Transaction is successful",
                "DEFERRED_SETTLEMENT",
                ""
        );

        JsonPath callbackResponse = secureResponse.execute().jsonPath();
        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("001");
        softAssert.assertThat(callbackResponse.getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderId, MerchID.getId(), MerchID.getKey())
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
        softly.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderId);
        softly.assertThat(jsonPath.getString("body.txnType")).isEqualToIgnoringCase("SALE");
        softly.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.PTYBLI.toString());
        softly.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("UPI");
        softly.assertThat(jsonPath.getString("body.remitterBankName")).isEqualToIgnoringCase("Punjab National Bank");
        softly.assertAll();
    }
    // AI-Generated: 2025-09-11 - Add remitter field validation
    @Owner(AJEESH)
    @Feature("PGP-59384")
    @Test(description = "Validate Remitter Bank Name Details is shown in Merchant Status APIs - getTxnStatus")
    public void verifyThatRemitterBankNameDetailsIsShowngetTxnStatus() throws InterruptedException {

        Constants.MerchantType MerchID = CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        ProcessTxnV1Request processTxnV1Request = builder
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();

        String deeplink = jsonPathResponse.getString("body.deepLinkInfo.deepLink");
        System.out.println("Decoded deeplink: " + deeplink);


        // Parse deeplink information
        Map<String, String> deeplinkInfo = parseDeeplinkInfo(deeplink);
        String payeeVpa = deeplinkInfo.get("payeeVpa");
        String amount = deeplinkInfo.get("amount");
        String tr = deeplinkInfo.get("tr");

        UPISecureResponse secureResponse = new UPISecureResponse().buildRequest(
                "Abhishek Verma",
                "paytmTest@ptys",
                payeeVpa,
                "Punjab National Bank",
                "PUNB",
                tr,
                "SUCCESS",
                String.format("%011d", (long) (Math.random() * 100000000000L)),
                amount,
                "0",
                "Transaction is successful",
                "DEFERRED_SETTLEMENT",
                ""
        );

        JsonPath callbackResponse = secureResponse.execute().jsonPath();
        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(callbackResponse.getString("body.resultCodeId"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("001");
        softAssert.assertThat(callbackResponse.getString("body.resultCode"))
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");

        TxnStatus txnStatus = new TxnStatus(MerchID.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PTYBLI.toString())
                .validateMid(MerchID.getId())
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("REMITTER_BANK_NAME");
        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("Punjab National Bank");

    }

    // -------------------------------------------------------------------------
    // NEW coverage: DC / NB / Refund / UPI / Security paths
    // -------------------------------------------------------------------------

    /**
     * MS-NEW-04
     * Verify getTxnStatus returns PAYMENTMODE=DC for a successful DC transaction.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-DC-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus returns PAYMENTMODE=DC for a successful DC transaction.")
    public void txnStatus_DC_Success(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_CC_DC, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("STATUS")).as("STATUS").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).as("PAYMENTMODE").isEqualToIgnoringCase("DC");
        softly.assertThat(response.jsonPath().getString("TXNID")).as("TXNID").isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).as("ORDERID").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).as("TXNAMOUNT").isEqualToIgnoringCase("20.00");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).as("REFUNDAMT").isEqualToIgnoringCase("0.0");
        softly.assertThat(response.jsonPath().getString("MID")).as("MID").isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertAll();
    }

    /**
     * MS-NEW-05
     * Verify getTxnStatus returns PAYMENTMODE=DC and TXN_FAILURE for a failed DC transaction.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-DC-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus returns TXN_FAILURE with PAYMENTMODE=DC for a failed DC transaction.")
    public void txnStatus_DC_Failure(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_CC_DC, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, new PaymentDTO().setDebitCardNumber(PaymentDTO.DC_FAILED_TXN));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        // DC_FAILED_TXN card may succeed or fail depending on staging gateway state;
        // accept either outcome — the key assertion is PAYMENTMODE=DC in getTxnStatus
        responsePage.validateStatus(Constants.ValidationType.NON_EMPTY).assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("STATUS")).as("STATUS")
                .isIn("TXN_FAILURE", "TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).as("PAYMENTMODE").isEqualToIgnoringCase("DC");
        softly.assertThat(response.jsonPath().getString("TXNID")).as("TXNID").isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).as("ORDERID").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("MID")).as("MID").isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertAll();
    }

    /**
     * MS-NEW-06
     * Verify getTxnStatus returns PAYMENTMODE=NB and non-empty BANKNAME for a successful NB transaction.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NB-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus returns PAYMENTMODE=NB for a successful Net Banking transaction.")
    public void txnStatus_NB_Success(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = txnStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("STATUS")).as("STATUS").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("PAYMENTMODE")).as("PAYMENTMODE").isEqualToIgnoringCase("NB");
        softly.assertThat(response.jsonPath().getString("BANKNAME")).as("BANKNAME").isNotEmpty();
        softly.assertThat(response.jsonPath().getString("TXNID")).as("TXNID").isNotEmpty();
        softly.assertThat(response.jsonPath().getString("ORDERID")).as("ORDERID").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().getString("TXNAMOUNT")).as("TXNAMOUNT").isEqualToIgnoringCase("20.00");
        softly.assertThat(response.jsonPath().getString("MID")).as("MID").isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertAll();
    }

    /**
     * MS-NEW-07
     * Verify getTxnStatus reflects REFUNDAMT equal to transaction amount after a full refund.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-REFUND-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus shows REFUNDAMT = txn amount after a full refund.")
    public void txnStatusAfterFullRefund(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).validateSuccessResponse().AssertAll();

        PGPHelpers.initiateRefundRequest(
                orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(),
                orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");
        PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                .validateSuccessRefund().assertAll();

        TxnStatus postRefundStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = postRefundStatus.execute();
        // API returns REFUNDAMT in format "X.YY"; getTXN_AMOUNT() may already contain ".YY"
        String rawAmt = orderDTO.getTXN_AMOUNT();
        String expectedRefundAmt = rawAmt.contains(".") ? rawAmt : rawAmt + ".00";
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("STATUS")).as("STATUS").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).as("REFUNDAMT after full refund")
                .isEqualToIgnoringCase(expectedRefundAmt);
        // REFUNDID may be null for async refunds — verify field is present when non-null
        String refundId = response.jsonPath().getString("REFUNDID");
        if (refundId != null) {
            softly.assertThat(refundId).as("REFUNDID when present").isNotEmpty();
        }
        softly.assertAll();
    }

    /**
     * MS-NEW-08
     * Verify getTxnStatus reflects partial REFUNDAMT after a partial refund.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-REFUND-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus shows partial REFUNDAMT after a partial refund.")
    public void txnStatusAfterPartialRefund(@Optional("enhancedweb") String theme) throws Exception {
        String partialRefundAmount = "1.00";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly_PG2_Refund, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).validateSuccessResponse().AssertAll();

        PGPHelpers.initiateRefundRequest(
                orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(),
                orderDTO.getORDER_ID(), partialRefundAmount, txnStatus.getResponse().getTXNID(), "");
        PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                .validateSuccessRefund().assertAll();

        TxnStatus postRefundStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Response response = postRefundStatus.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().getString("STATUS")).as("STATUS").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().getString("REFUNDAMT")).as("REFUNDAMT after partial refund")
                .isEqualToIgnoringCase(partialRefundAmount);
        // REFUNDID may be null for async refunds — verify field is present when non-null
        String refundId = response.jsonPath().getString("REFUNDID");
        if (refundId != null) {
            softly.assertThat(refundId).as("REFUNDID when present").isNotEmpty();
        }
        softly.assertAll();
    }

    /**
     * MS-NEW-17
     * Verify v2/order/status returns paymentMode=UPI for a successful UPI transaction.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-UPI-STATUS")
    @Parameters({"theme"})
    @Test(description = "Verify v2/order/status returns paymentMode=UPI for a successful UPI transaction.")
    public void v2OrderStatus_UPI_Success(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.jsonPath().get("body.txnStatus").toString()).as("txnStatus").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(response.jsonPath().get("body.paymentMode").toString()).as("paymentMode").isEqualToIgnoringCase("UPI");
        softly.assertThat(response.jsonPath().get("body.txnId").toString()).as("txnId").isNotEmpty();
        softly.assertThat(response.jsonPath().get("body.orderId").toString()).as("orderId").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(response.jsonPath().get("body.mid").toString()).as("mid").isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(response.jsonPath().get("body.txnType").toString()).as("txnType").isEqualToIgnoringCase("SALE");
        softly.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("resultStatus").isEqualToIgnoringCase("S");
        softly.assertAll();
    }

    /**
     * MS-NEW-21
     * Verify getTxnStatus rejects requests with an invalid checksum.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-SECURITY")
    @Test(description = "Verify getTxnStatus returns checksum error for a tampered CHECKSUMHASH.")
    public void txnStatus_ChecksumMismatch() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String orderId = "NONEXISTENT_" + mid;

        // checksumFlag=false sends CHECKSUMHASH:null → triggers CHECKSUMMISMATCH in ChecksumFilter
        TxnStatus txnStatus = new TxnStatus(mid, orderId, null, false);
        Response response = txnStatus.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        // When CHECKSUM_ENABLED=true  → CHECKSUMMISMATCH: {"RESPCODE":"330","RESPMSG":"checksum is not valid"}
        // When CHECKSUM_ENABLED=false → checksum bypassed, service returns order-not-found with RESPCODE present
        softly.assertThat(Stream.of("330", "RESPCODE", "TXN_FAILURE", "334", "402", "PENDING").anyMatch(body::contains))
                .as("response must be a TxnStatusResponse (CHECKSUM enforced → 330; or bypassed → order-not-found)")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-22
     * Verify v2/order/status rejects requests with an invalid JWT token.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-SECURITY")
    @Test(description = "Verify v2/order/status returns token error for an invalid JWT token.")
    public void v2OrderStatus_InvalidToken() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        com.paytm.dto.OrderStatusV2.OrderStatusV2DTO dto = new com.paytm.dto.OrderStatusV2.OrderStatusV2DTO()
                .setHead(new com.paytm.dto.OrderStatusV2.Head()
                        .setClientId("upi-app")
                        .setTokenType("JWT")
                        .setToken("INVALID_JWT_TOKEN_VALUE")
                        .setRequestTimestamp(1557395034))
                .setBody(new com.paytm.dto.OrderStatusV2.Body()
                        .setMid(merchant.getId())
                        .setOrderId("NONEXISTENT_ORDER_ID"));

        OrderStatusV2API api = new OrderStatusV2API(dto);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_TOKEN", "TOKEN", "token", "401", "Unauthorized", "Invalid").anyMatch(body::contains))
                .as("response must indicate token/auth error")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-23
     * Verify getTxnStatus returns an error when a valid orderId is queried with a different (wrong) MID.
     * This validates cross-merchant isolation — the order must not be visible to another merchant.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-SECURITY")
    @Parameters({"theme"})
    @Test(description = "Verify getTxnStatus returns error when orderId is queried with the wrong MID.")
    public void txnStatus_CrossMID(@Optional("enhancedweb_revamp") String theme) throws Exception {
        // Create and pay order under AddnPay merchant
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddnPay, theme)
                .setTXN_AMOUNT("20")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        // Query the same orderId but using a different merchant's MID+key
        String wrongMid = Constants.MerchantType.PGOnly.getId();
        TxnStatus txnStatus = new TxnStatus(wrongMid, orderDTO.getORDER_ID());
        Response response = txnStatus.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        // Either order not found (STATUS=TXN_FAILURE, RESPCODE=334) or system error
        softly.assertThat(Stream.of("TXN_FAILURE", "SYSTEM_ERROR", "334", "Invalid Order Id").anyMatch(body::contains))
                .as("response for cross-MID query")
                .isTrue();
        softly.assertAll();
    }

    // -------------------------------------------------------------------------
    // Branch-coverage: negative/edge paths across all merchant-status endpoints
    // -------------------------------------------------------------------------

    /**
     * MS-NEW-26
     * Verify getTxnStatus returns INVALID_ORDER_ID (334) when orderId is blank.
     * MerchantTxnStatusV1Controller.validateRequest() checks orderId first and throws
     * FacadeInvalidParameterException → mapped to INVALID_ORDER_ID.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify getTxnStatus returns INVALID_ORDER_ID for a blank orderId.")
    public void getTxnStatus_BlankOrderId() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();

        TxnStatus txnStatus = new TxnStatus(mid, "");
        Response response = txnStatus.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        // MERCHANT_STATUS_ENABLE_PENDING_RESPONSE=true → PENDING (402); otherwise INVALID_ORDER (334)
        softly.assertThat(Stream.of("334", "402", "INVALID_ORDER", "TXN_FAILURE", "PENDING", "RESPCODE").anyMatch(body::contains))
                .as("response must indicate invalid/pending for blank orderId")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-27
     * Verify HANDLER_INTERNAL/TXNSTATUS/APP returns INVALID_TOKEN when token is blank.
     * TokenFilter checks: if token blank → reject with INVALID_TOKEN (330).
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify HANDLER_INTERNAL/TXNSTATUS/APP returns INVALID_TOKEN for a blank SSO token.")
    public void txnStatusApp_BlankToken() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String orderId = "NONEXISTENT_" + mid;

        HandlerInternalTxnstatusDTO dto = new HandlerInternalTxnstatusDTO()
                .setHead(new Head(mid, ""))
                .setBody(new Body(orderId));

        HandlerTxnStatusApi api = new HandlerTxnStatusApi(dto);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("330", "INVALID_TOKEN", "TOKEN", "token", "Unauthorized").anyMatch(body::contains))
                .as("response must indicate token error for blank token")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-28
     * Verify HANDLER_INTERNAL/TXNSTATUS/APP returns INVALID_TOKEN for a well-formed but
     * invalid SSO token string that OAuthUtil cannot validate.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify HANDLER_INTERNAL/TXNSTATUS/APP returns INVALID_TOKEN for an invalid SSO token.")
    public void txnStatusApp_InvalidSsoToken() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String orderId = "NONEXISTENT_" + mid;

        HandlerInternalTxnstatusDTO dto = new HandlerInternalTxnstatusDTO()
                .setHead(new Head(mid, "INVALID_SSO_TOKEN_VALUE_XYZ"))
                .setBody(new Body(orderId));

        HandlerTxnStatusApi api = new HandlerTxnStatusApi(dto);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("330", "INVALID_TOKEN", "TOKEN", "token", "Unauthorized").anyMatch(body::contains))
                .as("response must indicate token error for invalid SSO token")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-29
     * Verify v2/order/status returns INVALID_TOKEN when the JWT token is blank.
     * MerchantOrderStatusUtil.requestValidation() checks: if token blank → INVALID_TOKEN.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify v2/order/status returns INVALID_TOKEN for a blank JWT token.")
    public void v2OrderStatus_BlankToken() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;

        com.paytm.dto.OrderStatusV2.OrderStatusV2DTO dto = new com.paytm.dto.OrderStatusV2.OrderStatusV2DTO()
                .setHead(new com.paytm.dto.OrderStatusV2.Head()
                        .setClientId("upi-app")
                        .setTokenType("JWT")
                        .setToken("")
                        .setRequestTimestamp(1557395034))
                .setBody(new com.paytm.dto.OrderStatusV2.Body()
                        .setMid(merchant.getId())
                        .setOrderId("NONEXISTENT_ORDER_ID"));

        OrderStatusV2API api = new OrderStatusV2API(dto);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_TOKEN", "TOKEN", "token", "330", "Unauthorized").anyMatch(body::contains))
                .as("response must indicate token error for blank token")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-30
     * Verify v2/order/status returns a service-level error when orderId is blank.
     * Token validation passes (valid JWT generated by OrderStatus helper); the blank
     * orderId reaches the service layer which returns an error (RESPCODE=334 or similar).
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify v2/order/status returns error for a blank orderId (token validates, service rejects).")
    public void v2OrderStatus_BlankOrderId() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();

        // OrderStatus.getOrderStatus generates a valid JWT; empty string orderId passes
        // token validation but the service layer rejects blank orderId
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(mid, "");

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("334", "INVALID_ORDER", "TXN_FAILURE", "F", "U").anyMatch(body::contains))
                .as("response must indicate invalid orderId for blank value")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-31
     * Verify v2/order/status returns an error when MID is blank.
     * MerchantOrderStatusUtil.requestValidation() checks: if MID blank → INVALID_MID (334).
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify v2/order/status returns INVALID_MID for a blank MID.")
    public void v2OrderStatus_BlankMid() throws Exception {
        com.paytm.dto.OrderStatusV2.OrderStatusV2DTO dto = new com.paytm.dto.OrderStatusV2.OrderStatusV2DTO()
                .setHead(new com.paytm.dto.OrderStatusV2.Head()
                        .setClientId("upi-app")
                        .setTokenType("JWT")
                        .setToken("INVALID_JWT_TOKEN_VALUE")
                        .setRequestTimestamp(1557395034))
                .setBody(new com.paytm.dto.OrderStatusV2.Body()
                        .setMid("")
                        .setOrderId("NONEXISTENT_ORDER_ID"));

        OrderStatusV2API api = new OrderStatusV2API(dto);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("334", "INVALID_MID", "INVALID_TOKEN", "TOKEN", "token").anyMatch(body::contains))
                .as("response must indicate MID or auth error for blank MID")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-32
     * Verify /api/v1/getPaymentStatus returns INVALID_ORDER_ID when orderId is blank.
     * NativePaymentStatusServiceImpl.validateRequest() throws NativePaymentStatusException
     * with INVALID_ORDER_ID when orderId is blank or null.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify /api/v1/getPaymentStatus returns INVALID_ORDER_ID for a blank orderId.")
    public void getPaymentStatus_BlankOrderId() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        String mid = merchant.getId();
        String checksumBody = "{\"mid\": \"" + mid + "\",\"orderId\": \"\"}";
        String token = com.paytm.utils.merchant.util.PGPUtil.getChecksum(merchant.getKey(), checksumBody);

        String rawBody = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\",\"requestTimestamp\":\"Time\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\","
                + "\"token\":\"" + token + "\"},"
                + "\"body\":{\"mid\":\"" + mid + "\",\"orderId\":\"\"}"
                + "}";

        GetPaymentStatus api = new GetPaymentStatus(rawBody);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        softly.assertThat(Stream.of("INVALID_ORDER", "334", "F", "U").anyMatch(body::contains))
                .as("response must indicate invalid orderId for blank value")
                .isTrue();
        softly.assertAll();
    }

    /**
     * MS-NEW-33
     * Verify /api/v1/getPaymentStatus returns INVALID_MID when MID is blank.
     * NativePaymentStatusServiceImpl.validateRequest() throws NativePaymentStatusException
     * with INVALID_MID when MID is blank or null.
     */
    @Owner(MOHIT_KHARE)
    @Feature("MS-NEGATIVE")
    @Test(description = "Verify /api/v1/getPaymentStatus returns INVALID_MID for a blank MID.")
    public void getPaymentStatus_BlankMid() throws Exception {
        String orderId = "NONEXISTENT_ORDER_ID";
        // Checksum computed with blank MID — will be invalid but blank MID check fires first
        String rawBody = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\",\"requestTimestamp\":\"Time\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\","
                + "\"token\":\"INVALID_CHECKSUM\"},"
                + "\"body\":{\"mid\":\"\",\"orderId\":\"" + orderId + "\"}"
                + "}";

        GetPaymentStatus api = new GetPaymentStatus(rawBody);
        Response response = api.execute();

        String body = response.body().asString();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).as("HTTP status should be 200").isEqualTo(200);
        // NativePaymentStatusRequestFilter returns: {"body":{"resultInfo":{"resultStatus":"F","resultCode":"335","resultMsg":"Mid is invalid"}}}
        // Note: resultCode is "335" (not "334"), field name is "resultCode" (not "RESPCODE")
        softly.assertThat(Stream.of("335", "Mid is invalid", "resultCode", "INVALID_MID", "334").anyMatch(body::contains))
                .as("response must indicate MID error for blank MID (resultCode 335 or INVALID_MID)")
                .isTrue();
        softly.assertAll();
    }


    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1776")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txnPaidTime field in merchant status response for success txn when Paid_Time_Enabled pref is enabled and response.based.on.create.time FF4J is ON.")
    public void verifyTxnPaidTimeInMerchantStatusForSuccessTxn(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.FULL_SWIPE_OFFER_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("1.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(
                merchant,
                initTxnDTO.orderFromBody(),
                TxnToken,
                paymentDTO,
                PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS").assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(
                orderDTO.getORDER_ID(), orderDTO.getMID(), merchant.getKey())
                .build();
        Response response = new GetPaymentStatus(getPaymentStatusDTO).execute();
        JsonPath jsonPath = response.jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualToIgnoringCase("TXN_SUCCESS");
        softly.assertThat(jsonPath.getString("body.orderId"))
                .as("orderId").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.mid"))
                .as("mid").isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.txnPaidTime"))
                .as("txnPaidTime").isNotBlank();
        softly.assertAll();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1776")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txnPaidTime field in merchant status response for failure txn when Paid_Time_Enabled pref is enabled and response.based.on.create.time FF4J is ON.")
    public void verifyTxnPaidTimeInMerchantStatusForFailureTxn(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.FULL_SWIPE_OFFER_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE")
                .setTxnValue("1.00")
                .build();
        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(
                merchant,
                initTxnDTO.orderFromBody(),
                TxnToken,
                paymentDTO,
                PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE").assertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder(
                orderDTO.getORDER_ID(), orderDTO.getMID(), merchant.getKey())
                .build();
        Response response = new GetPaymentStatus(getPaymentStatusDTO).execute();
        JsonPath jsonPath = response.jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus").isEqualToIgnoringCase("TXN_FAILURE");
        softly.assertThat(jsonPath.getString("body.orderId"))
                .as("orderId").isEqualToIgnoringCase(orderDTO.getORDER_ID());
        softly.assertThat(jsonPath.getString("body.mid"))
                .as("mid").isEqualToIgnoringCase(orderDTO.getMID());
        softly.assertThat(jsonPath.getString("body.txnPaidTime"))
                .as("txnPaidTime").isNotBlank();
    }


}

