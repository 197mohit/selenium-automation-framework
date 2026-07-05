package scripts.api.UPI;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.CloseOrder;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.*;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.UpdateTransaction.UpdateTransactionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.PGPHelpers.assertRefundSuccessNotifyPresence;
import static io.restassured.RestAssured.given;

public class UPItransactionPG2 extends PGPBaseTest {

    private static final String API_VERSION_V2 = "v2";
    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
    private final OopsPage oopsPage = new OopsPage();
    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
    private final String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
    private final String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|orderAlreadyCreated:{orderAlreadyCreated}";
    private final String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }
    public void assertPTCCommonResponse(ProcessTxnV1Response ptcResponse, InitTxnDTO initTxnDTO){
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getCURRENCY()).isEqualTo("INR");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getMID()).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getORDERID()).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT()).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNDATE()).isNotNull();
    }
    public void assertTheiaTxnStatusCommonResponse(Response theiaTxnStatusResponse, InitTxnDTO initTxnDTO, Constants.ResponseCode responseCode){
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(initTxnDTO.getBody().getMid());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo(responseCode.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo(responseCode.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
    }

    public void verifyEsnLength(InitTxnDTO initTxnDTO, int length) throws InterruptedException {
        String grepEsn = "grep \"" + initTxnDTO.getBody().getOrderId() +"\" "+ LocalConfig.INSTAPROXY_LOGS+" |grep \"ExtSN=\" | grep \"Payment Request\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="),extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        Assertions.assertThat(extSnValue.length()).isEqualTo(length);
    }

    public void validateAgainRetrygivesorderispaid(String orderId) throws InterruptedException {
        String grepAgainRetrygivesorederis_paid= "grep " + orderId +" "+ LocalConfig.THEIA_FACADE_LOGS +
                "|" + "grep \"ACQUIRING_PAY_ORDER\" | grep \"RESPONSE\"";
        String grepRetryagaingivesorderispaid = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepAgainRetrygivesorederis_paid);
        Assertions.assertThat(grepRetryagaingivesorderispaid)
                .contains("\"resultCode\":\"ORDER_IS_PAID\"");
    }

    public static String generateQRViaWallet(Constants.MerchantType merchantType)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"");
        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate Success UPI push transaction for PG2 MID routed through PG2")
    public void validateSuccessUpiPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate Failed UPI push transaction for PG2 MID routed through PG2")
    public void validateFailedUpiPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).setTxnValue("39")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());

        verifyEsnLength(initTxnDTO ,19);

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(Constants.MerchantType.UPIPUSHPG2.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate Pending UPI push transaction for PG2 MID routed through PG2, close order V1 and then check merchant status")
    public void pendingUpiPushTxnPG2MIDCloseOrderV1ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.UPIPUSHPG2.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_STATUS_PENDING);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(MerchantType.UPIPUSHPG2.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate Pending UPI push transaction for PG2 MID routed through PG2, close order V2 and then check merchant status")
    public void pendingUpiPushTxnPG2MIDCloseOrderV2ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getBANKTXNID()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        String txnId = ptcResponse.getBody().getTxnInfo().getTXNID();
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.UPIPUSHPG2.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_STATUS_PENDING);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());

        CloseOrderV2Api closeOrderV2Api = new CloseOrderV2Api("TXN_TOKEN", MerchantType.UPIPUSHPG2.getId(), initTxnDTO.getBody().getOrderId(), initTxnResponse.getBody().getTxnToken(), "true");
        Response closeOrderResponse = closeOrderV2Api.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.CURRENCY")).isEqualTo("INR");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.MID")).isEqualTo(MerchantType.UPIPUSHPG2.getId());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.RESPCODE")).isEqualTo("141");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("User has not completed transaction.");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.getBody().getTxnAmount().getValue()));
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.txnInfo.TXNID")).isEqualTo(txnId);

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate Pending UPI push transaction for PG2 MID routed through PG2, execute Theia close order and then check merchant status")
    public void pendingUpiPushTxnPG2MIDTheiaCloseOrderThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.UPIPUSHPG2.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_STATUS_PENDING);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());

        CloseOrder closeOrder = new CloseOrder(initTxnDTO.getBody().getOrderId(),MerchantType.UPIPUSHPG2.getId(),true);
        Response closeOrderResponse =  closeOrder.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("statusMessage")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("status")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("statusCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate that if user id is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void userNotPrsentInFF4JUPIPushTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,20);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.UPIPUSHPG2.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate that if mid is not present in FF4J Flag theia.payment.adapter.feature but pref is enabled on mid then txn should be routed to PG2, txn is pending then close order")
    public void midNotPrsentInFF4JButPrefEnabledUPIPushTxnRoutedToPG2PendingTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2FF4JNOPREFYES).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPUSHPG2FF4JNOPREFYES.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2FF4JNOPREFYES.getId())
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.UPIPUSHPG2FF4JNOPREFYES.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_STATUS_PENDING);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(MerchantType.UPIPUSHPG2FF4JNOPREFYES.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(MerchantType.UPIPUSHPG2FF4JNOPREFYES.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate that if user id is present but mid is not present in FF4J Flag theia.payment.adapter.feature then txn should be routed to P+")
    public void userPrsentMidNotPresentInFF4JUPIPushTxnRoutedToPPlus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPG2FF4JDISABLED)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPIPG2FF4JDISABLED.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,20);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.UPIPG2FF4JDISABLED.getId())
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate that if mid is present in FF4J Flag theia.payment.adapter.feature but not present in theia.pg2.enabled, merchantStatus.pg2.enabled and pref is disabled on mid then close order, theia status and merchant status should fail")
    public void midNotPresentInFF4JTheiaStatusPrefDisabledCloseOrderFail() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2FF4JYESPREFNO.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2FF4JYESPREFNO).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,19);

        //  merchantStatus.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but merchant status will route to P+ because of flag off. so it will show , invalid orderId
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.INVALID_ORDERID.getRespCode())
                .validateRespMsg(Constants.ResponseCode.INVALID_ORDERID.getRespMsg())
                .validateMid(mid)
                .AssertAll();

        // theia.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but theia status will route to P+ because of flag off. so it will show , Txn Failed
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.PAYMENT_FAILED);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("FAILED");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());

        //  theia.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but close order will route to P+ because of flag off. so it will show , invalid Request
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Request");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("02");
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate UPI push PG2 transaction full Async Refund")
    public void upiPushPG2FullAsyncRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.UPIPUSHPG2.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.UPIPUSHPG2, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate UPI push PG2 transaction partial Async Refund")
    public void upiPushPG2PartialAsyncRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.UPIPUSHPG2.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        String refundAmount = "1";
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.UPIPUSHPG2, refundAmount, initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-37618")
    @Test(description = "Validate that if mid is not present in FF4J flag refund.pg2.enabled then refund should fail")
    public void midNotPresentInRefundFF4J() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2FF4JYESPREFNO.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2FF4JYESPREFNO)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO ,19);

        //  merchantStatus.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but merchant status will route to P+ because of flag off. so it will show , invalid orderId
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.INVALID_ORDERID.getRespCode())
                .validateRespMsg(Constants.ResponseCode.INVALID_ORDERID.getRespMsg())
                .validateMid(mid)
                .AssertAll();

        // refund.pg2.enabled flags is off for given mid with theia.payment.adapter.feature flag on. txn is done on PG2 but refund will route to P+ because of flag off. so it will show , invalid refund request
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2FF4JYESPREFNO, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        asyncRefundResp.prettyPrint();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.INVALID_REFUND_REQUEST.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.INVALID_REFUND_REQUEST.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38757")
    @Test(description = "Validate Success Co Then Pay UPI push transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYSuccessCoThenPayUpiPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38757")
    @Test(description = "Validate Failed Co Then Pay UPI push transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYFailedCoThenPayUpiPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("39")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        verifyEsnLength(initTxnDTO ,19);

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38757")
    @Test(description = "Validate Pending Co Then Pay UPI push transaction for PG2 MID routed through PG2, close order V1 and then check merchant status")
    public void pendingFullTrafficYCoThenPayUpiPushTxnPG2MIDCloseOrderV1ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();
        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.PRAMOD_KUMAR)
    @Feature("PG2-4055")
    @Test(description = "Validate UpiPushPG2 FullAsyncRefund for FullTraffic Y Success CoThenPay ")
    public void validateFullTrafficYSuccessCoThenPayUpiPushPG2FullAsyncRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, initTxnDTO.txnAmountFromBody(), initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println(asyncRefundResp);
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(initTxnDTO.txnAmountFromBody());
    }
    @Owner(Constants.Owner.PRAMOD_KUMAR)
    @Feature("PG2-4055")
    @Test(description = "Validate Upi Push PG2PartialAsyncRefund for FullTraffic Y success CoThenPay")
    public void validateFullTrafficYSuccessCoThenPayUpiPushPG2PartialAsyncRefund() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("5")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
        SyncRefund syncRefund = new SyncRefund();
        String refundAmount="1";
        Response asyncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, refundAmount, initTxnDTO.getBody().getOrderId(),
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println(asyncRefundResp);
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(refundAmount);
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4103")
    @Test(description = "Validate Success COP UPI push transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYSuccessCOPUpiPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PGOnly)
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4103")
    @Test(description = "Validate Failed COP UPI push transaction for PG2 MID routed through PG2")
    public void validateFullTrafficYFailedCOPUpiPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("39")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        verifyEsnLength(initTxnDTO ,19);
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId()));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4103")
    @Test(description = "Validate Pending COP UPI push transaction for PG2 MID routed through PG2, close order V1 and then check merchant status")
    public void pendingFullTrafficCOPUpiPushTxnPG2MIDCloseOrderV1ThenCheckMerchantStatus() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WAP").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateMid(mid)
                .AssertAll();
    }


    @Owner(VIKASH_VERMA)
    @Feature("PG2-4498")
    @Test(description = "PG2 COP Initiate Txn is executed with Amt 39, UPI failed TXn is done PTC is executed again with Wallet")
    public void validateFailedUpitxnretrywithwallet() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,100.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry).setTxnValue("39")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());

        verifyEsnLength(initTxnDTO ,19);

        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();

        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_SUCCESS);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4547")
    @Test(description = "Validate Success COP UPI push Order Modify transaction for PG2 MID routed through PG2")
    public void validateFullTrafficCOPUPIPUshOrderModifyTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("5")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));

        updateTransactionDTO.setChecksum(MerchantType.PG2_COP_FULL_TRAFFIC_Y.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT().equals(txnAmount));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();

        String orderId=ptcResponse.getBody().getTxnInfo().getORDERID();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT").equals(txnAmount));

    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-4725")
    @Test(description = "PG2 CO Then pay Initiate Txn is executed with Amt 39, UPI failed TXn is done PTC is executed again with Wallet")
    public void validateCOTPFailedUpitxnretrywithwallet() throws Exception{
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        WalletHelpers.modifyBalance(user,100.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry).setTxnValue("39")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());
        verifyEsnLength(initTxnDTO ,19);
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO, 19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry.getId()).setOrderId(initTxnDTO.getBody().getOrderId()))
                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response theiaTxnStatusResponse = transactionStatusV1API.execute();
        assertTheiaTxnStatusCommonResponse(theiaTxnStatusResponse, initTxnDTO, Constants.ResponseCode.TXN_SUCCESS);
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(theiaTxnStatusResponse.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4636")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Success UPI PUSH transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPaySuccessUPIPUSHTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant) .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,20);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4636")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Failed UPI PUSH transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPayFailUPIPUSHTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("39")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_FAILURE.toString());

        verifyEsnLength(initTxnDTO ,20);

        // adding this code to manually close the order when order is not getting closed for some txn.
        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WEB").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        closeOrderAPI.execute();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4636")
    @Test(description = "Validate PG2_ENABLED Co Then Pay Pending UPI PUSH transaction for PG2 MID routed through P+")
    public void validatePG2EnabledCoThenPayPendingUPIPUSHTxnPG2MID() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("52")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_ENABLED_CO_THEN_PAY.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo(Constants.ResponseCode.TXN_STATUS_PENDING.getRespCode());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo(Constants.ResponseCode.PTC_TXN_PENDING.getRespMsg());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).isNotNull();

        verifyEsnLength(initTxnDTO ,20);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.PENDING.toString())
                .validateRespCode(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespCode())
                .validateRespMsg(Constants.ResponseCode.MERCHANT_STATUS_TXN_PENDING.getRespMsg())
                .validateMid(mid)
                .AssertAll();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setHead(new com.paytm.dto.CloseOrder.Head().setChannelId("WEB").setVersion("v1").setSigature(""))
                .setBody(new com.paytm.dto.CloseOrder.Body().setIsForceClose(true).setOrderId(initTxnDTO.getBody().getOrderId()).setMid(mid));
        CloseOrderAPI closeOrderAPI = new CloseOrderAPI(closeOrderDTO);
        Response closeOrderResponse = closeOrderAPI.execute();
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(closeOrderResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("01");

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_FAILURE.toString())
                .validateRespCode(Constants.ResponseCode.PAYMENT_FAILED.getRespCode())
                .validateRespMsg(Constants.ResponseCode.PAYMENT_FAILED.getRespMsg())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(VIKASH_VERMA)
    @Feature("PG2-4832")
    @Test(description = "PG2 COP Initiate Txn is executed with Amt 1, UPI Txn is done PTC is executed again with UPI")
    public void validateSuccessUpiPushTxnRetrywithupi() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry).setTxnValue("1")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        verifyEsnLength(initTxnDTO ,19);
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        assertPTCCommonResponse(ptcResponse1, initTxnDTO);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse1.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        CommonHelpers.verifyEsnLength(initTxnDTO.getBody().getOrderId(),19);
        String orderId=ptcResponse.getBody().getTxnInfo().getORDERID();
        validateAgainRetrygivesorderispaid(orderId);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(MerchantType.PG2_COP_FULL_TRAFFIC_Y_Retry.getId())
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-4845")
    @Test(description = "PG2 COP Initiate Txn is executed with Amt 5, Postpaid TXn is done . Order Modify is done for 6 rs along with good info, shipping info, ")
    public void validateFullTrafficCOPUPIPUshOrderModifyaftersuccessTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_COP_FULL_TRAFFIC_Y).setTxnValue("5")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();

        String orderId=ptcResponse.getBody().getTxnInfo().getORDERID();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));

        updateTransactionDTO.setChecksum(MerchantType.PG2_COP_FULL_TRAFFIC_Y.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        Response updateTransactionresponse = updateTransaction.execute();
        updateTransactionresponse.prettyPrint();

        Assertions.assertThat(updateTransactionresponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(updateTransactionresponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1111");
        Assertions.assertThat(updateTransactionresponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("transaction already in process");
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4562")
    @Test(description = "Validate Success Co Then Pay Simplified Promo Order Modify UPI push transaction for PG2 MID routed through PG2")
    public void validateCoThenPaySimplifiedPromoOrderModifyUPIPushTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();
        Promo promocode = new Promo(true);
        new Merchant(mid, true ).getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y,simplifiedPaymentOffers).setTxnValue("100")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        String discountedTxnAmmount= ptcResponse.getBody().getTxnInfo().getTXNAMOUNT();
        verifyEsnLength(initTxnDTO ,19);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(discountedTxnAmmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();
        String orderID= ptcResponse.getBody().getTxnInfo().getORDERID();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderID))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
    }
    @Owner(PRAMOD_KUMAR)
    @Feature("PG2-4800")
    @Test(description = "Validate Success CO then pay UPI push Order Modify transaction for PG2 MID routed through PG2")
    public void validateFullTrafficCOThenPayUPIPUshOrderModifyTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y).setTxnValue("5")
                .setOrderId(LocalConfig.UPI_ORDERWITH_ENV_NAME+CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId();

        Double updatedTxnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1;
        TxnAmount txnAmount = initTxnDTO.getBody().getTxnAmount();
        txnAmount.setValue(String.valueOf(updatedTxnAmount));
        UpdateTransactionDTO updateTransactionDTO = new UpdateTransactionDTO();
        updateTransactionDTO.setHead(new com.paytm.dto.NativeDTO.UpdateTransaction.Head(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.UpdateTransaction.Body().setExtendInfo(initTxnDTO.getBody().getExtendInfo())
                        .setGoods(Arrays.asList(initTxnDTO.getBody().getGoods().clone()))
                        .setShippingInfo(initTxnDTO.getBody().getShippingInfo())
                        .setTxnAmount(txnAmount));

        updateTransactionDTO.setChecksum(MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getKey());
        UpdateTransaction updateTransaction = new UpdateTransaction(updateTransactionDTO, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        updateTransaction.execute();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNAMOUNT().equals(txnAmount));
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        verifyEsnLength(initTxnDTO ,19);
        String updatedTxnAmount1=ptcResponse.getBody().getTxnInfo().getTXNAMOUNT();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(updatedTxnAmount1)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();

        String orderId=ptcResponse.getBody().getTxnInfo().getORDERID();
        TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
        transactionStatusV1DTO.setBody(new Body().setIsCallbackUrlRequired(true).setIsFinalTxnStatusRequired(false).setMid(mid).setOrderId(orderId))
                .setHead(new Head().setVersion(API_VERSION_V2).setTokenType("TXN_TOKEN").setToken(txnToken));
        TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
        Response response = transactionStatusV1API.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.TXNAMOUNT").equals(txnAmount));
    }

    @Owner(CHAKSHU)
    @Feature("PG2-7353")
    @Test(description = "PG2 COP Static QR UPI PUSH Scan n Pay Txn")
    public void validatePG2COPStaticQRUPIPushScanNPay() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        com.paytm.dto.processTransactionV1.TxnAmount txnAmount = new com.paytm.dto.processTransactionV1.TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        // generating QR code ID for the merchant
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderAlreadyCreated")).as("Static QR configuration is incorrect").isEqualTo(null);
        Assertions.assertThat(fetchQRResponse.getString("body.qrInfo.response.orderQr")).as("Static QR configuration is incorrect").isEqualTo("false");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        String mid = merchant.getId();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "false"));

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("NATIVE")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setTxnAmount(txnAmount)
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo1)
                .setExtendInfo(extendInfo)
                .setPayerAccount("9972746530@paytm")
                .setSeqNumber("PTMc925e92cd14944dc89c33727564e5430")
                .setUpiAccRefId("222907")
                .setQRCodeId(qrCodeId)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).contains("0000");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.getValue())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(mid)
                .AssertAll();

        String paymentnotifyRequest = "grep " + orderId + " " + LocalConfig.PGPROXY_LOGS +
                " | grep \"request\" | grep \"alipayplus.acquiring.order.paymentNotify\"";
        String paymentnotifyRequestLogs = Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, paymentnotifyRequest), s -> !"".equals(s));
        Assertions.assertThat(paymentnotifyRequestLogs).
                contains("\"resultStatus\":\"S\"")
                . contains("\"resultCode\":\"SUCCESS\"")
                .contains("\"resultMsg\":\"SUCCESS\"");

        SyncRefund asyncRefund = new SyncRefund();
        Response asyncRefundResp = given().spec(asyncRefund.reqSpecAsyncRefund(merchant,txnAmount.getValue(), orderId,
                        txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("Async Refund Response " + asyncRefundResp.body().prettyPrint());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespCode());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo(Constants.ResponseCode.REFUND_PENDING.getRespMsg());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(asyncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(txnAmount.getValue());

        assertRefundSuccessNotifyPresence(orderId);
    }


    @Owner(CHAKSHU)
    @Feature("PG2-7353")
    @Test(description = "PG2 COP UPI PSP Txn")
    public void validatePG2COPUPIPSP() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        String txnAmount = "100";

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount)
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();
        String esn = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        Assertions.assertThat(esn.length()).isEqualTo(19);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(CHAKSHU)
    @Feature("PG2-14132")
    @Test(description = "Validate ccOnUPIAllowed true, ccOnUPIAllowedForAddNPay false and merchantEligibleUPICC true for ADDANDPAY Payment Flow  when CC_ON_UPI_RAILS_ENABLED is Y and wallet is deselected")
    public void validateccOnUPIAllowedccOnUPIAllowedForAddNPayandwalletdeselectedwhenCCONUPIRAILSENABLEDisY() throws Exception{
            User user = userManager.getForRead(Label.PG2WALLETUSER);
            WalletHelpers.modifyBalance(user,1000.0);
            Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2000").build();
            InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
            String txnToken = initTxnResponse.getBody().getTxnToken();
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                    initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowedForAddNPay")).isEqualTo("false");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");

            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                    merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                    .setPaymentMode("UPI")
                    .setPreferredOtpPage("bank")
                    .setAuthMode("USRPWD")
                    .setChannelCode("push")
                    .setChannelId("WAP")
                    .setMpin(mpin)
                    .setRiskExtendInfo(riskExtendedInfo)
                    .setCreditBlock(creditblock)
                    .build();
            ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
            assertPTCCommonResponse(ptcResponse, initTxnDTO);
            Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
            Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
            Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
            Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        System.out.println( passThroughExtendInfologs);
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"true\"");
}

    @Owner(CHAKSHU)
    @Feature("PG2-14132")
    @Test(description = "Validate ccOnUPIAllowed true, ccOnUPIAllowedForAddNPay false and merchantEligibleUPICC false for ADDANDPAY Payment Flow  when CC_ON_UPI_RAILS_ENABLED is Y and wallet is selected")
    public void validateccOnUPIAllowedccOnUPIAllowedForAddNPayandwalletselectedwhenCCONUPIRAILSENABLEDisY() throws Exception{
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1000.0);
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowedForAddNPay")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPaymentFlow("ADDANDPAY")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"false\"");
    }

    @Owner(CHAKSHU)
    @Feature("PG2-14132")
    @Test(description = "Validate ccOnUPIAllowed true ccOnUPIAllowedForAddNPay false for NONE Payment Flow  when CC_ON_UPI_RAILS_ENABLED is Y")
    public void validateccOnUPIAllowedccOnUPIAllowedwhenCCONUPIRAILSENABLEDisY() throws Exception{
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1000.0);
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowedForAddNPay")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).isEqualTo("NONE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setCreditBlock(creditblock)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PG2-14132")
    @Test(description = "Validate ccOnUPIAllowed false, ccOnUPIAllowedForAddNPay false and merchantEligibleUPICC false for ADDANDPAY Payment Flow  when CC_ON_UPI_RAILS_ENABLED is N and wallet is selected")
    public void validateccOnUPIAllowedccOnUPIAllowedForAddNPayandwalletselectedwhenCCONUPIRAILSENABLEDisN() throws Exception{
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1000.0);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowedForAddNPay")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPaymentFlow("ADDANDPAY")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"false\"");
    }

    @Owner(CHAKSHU)
    @Feature("PG2-14132")
    @Test(description = "Validate ccOnUPIAllowed false, ccOnUPIAllowedForAddNPay false and merchantEligibleUPICC false for ADDANDPAY Payment Flow  when CC_ON_UPI_RAILS_ENABLED is N and wallet is deselected")
    public void validateccOnUPIAllowedccOnUPIAllowedForAddNPayandwalletdeselectedwhenCCONUPIRAILSENABLEDisN() throws Exception{
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        WalletHelpers.modifyBalance(user,1000.0);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowedForAddNPay")).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponse(ptcResponse, initTxnDTO);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"false\"");
    }

    @Owner(HARSHITA)
    @Feature("PGP-49001")
    @Test(description = "Validate UPI Push DQR transaction, merchant-status, refund, peon")
    public void PGP_49001_TC01() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = MerchantType.STAGING_MID1.getId();
        String amount = "10";
//        WalletHelpers.modifyBalance(user, Double.parseDouble(amount));
        PGPHelpers.validateRefundAllowedWithChecksum(mid);
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(MerchantType.STAGING_MID1, amount, orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .rootPath("body")
                .body("resultInfo.resultStatus", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("resultInfo.resultCode", Matchers.equalToIgnoringCase("QR_0001"))
                .body("resultInfo.resultMsg", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("qrCodeId", Matchers.notNullValue())
                .body("qrData", Matchers.notNullValue())
                .extract().jsonPath()
                .getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId, amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("NATIVE")
                .setWebsite("retail")
                .setPaymentMode("BALANCE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendedInfo1)
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).contains("0000");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(amount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(mid)
                .AssertAll();

        SyncRefund syncRefund = new SyncRefund();
        Response syncRefundResp = given().spec(syncRefund.reqSpecAsyncRefund(MerchantType.STAGING_MID1, amount, orderId,
                txnStatus.getResponse().getTXNID()))
                .post().then().extract().response();
        System.out.println("sync Refund Response " + syncRefundResp.body().prettyPrint());
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.mid")).isEqualTo(mid);
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.refId")).isNotNull();
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo(Constants.TXNSTATUS.PENDING.toString());
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.refundId")).isNotNull();
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.txnId")).isEqualTo(ptcResponse.getBody().getTxnInfo().getTXNID());
        Assertions.assertThat(syncRefundResp.jsonPath().getString("body.refundAmount")).isEqualTo(amount);
        //String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway,orderId,"Type=PeonSentSerivce");
        //Assertions.assertThat(logs).contains("Peon Sent successfully to MID: "+ mid + " for " + "OrderId:" + orderId);
    }

    @Owner(HARSHITA)
    @Feature("PGP-49001")
    @Test(description = "Validate saved Card txn")
    public void PGP_49001_TC02() throws Exception {
            User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
            Constants.MerchantType merchantType = MerchantType.STAGING_MID;
            String orderID=CommonHelpers.generateOrderId();
            String paymentMode = "CREDIT_CARD";
            String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
            SavedCardHelpers.addCardOnMidCustId(merchantType,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
            String tin= SavedCardHelpers.getTin();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchantType,user.ssoToken(), orderID)
                    .setCustId(custId)
                    .setTxnValue("10")
                    .setSsoToken("")
                    .setOrderId(orderID)
                    .build();
            InitTxn initTxn = new InitTxn(initTxnDTO);
            Response response = initTxn.execute();
            Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
            String txnToken = response.jsonPath().get("body.txnToken").toString();
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                    initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
            Assertions.assertThat(fetchPaymentOptionsJson.getString(
                   "body.merchantPayOption.savedInstruments.findAll { it.channelCode=='MASTER'}.cardDetails.cardId").replaceAll("\\[|\\]", "")).isEqualTo(tin);
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(),  txnToken,initTxnDTO.orderFromBody()).setCardInfo(tin+"||347|")
                    .setPaymentMode(paymentMode).build();
            ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
            Response ptcResponse = processTransactionV1.execute();
            Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
            NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
            ResponsePage responsePage = new ResponsePage();
            responsePage.validateStatus("TXN_SUCCESS");
            responsePage.assertAll();
    }

    @Owner(HARSHITA)
    @Feature("PGP-49001")
    @Parameters({"theme"})
    @Test(description = "To verify response of Subscription transaction")
    public void PGP_49001_TC03(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_UI_TEXT;
        String TxnAmount = "2";
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchant, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .assertAll();
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf1("abc");
        extendInfo.setUdf2("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:vivek3|udf1:abc|udf2:jkl|orderAlreadyCreated:true");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoUDF1UDF3UpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf1("abc");
        extendInfo.setUdf3("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:jkl|udf1:abc|udf2:vivek2|orderAlreadyCreated:true");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoUDF3UpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf3("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:jkl|udf1:vivek1|udf2:vivek2|orderAlreadyCreated:true|pushDataToDynamicQr:true");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void xvalidateMultipleObjectinAdditionalInfoMercUnqRefUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setMercUnqRef("abc");
        extendInfo.setUdf3("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:abc|sdkType:AIO_SDK_PG|udf3:jkl|udf1:vivek1|udf2:vivek2|orderAlreadyCreated:true");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectwithoutAdditionalInfoUDF3UpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        FF4JFlags.enable(FF4JFeatures.CREATE_ORDER_IN_INTTXN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setMercUnqRef("abc");
        extendInfo.setUdf3("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:abc|sdkType:AIO_SDK_PG|udf3:jkl|udf1:vivek1|udf2:vivek2\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectemptyAdditionalInfoUDF3UpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setMercUnqRef("abc");
        extendInfo.setUdf3("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:abc|sdkType:AIO_SDK_PG|udf3:jkl|udf1:vivek1|udf2:vivek2\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validatenoExtendinfoUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setMercUnqRef("abc");
        extendInfo.setUdf3("jkl");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:abc|sdkType:AIO_SDK_PG|udf3:jkl|udf1:vivek1|udf2:vivek2\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validatemptyExtendinfoUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ExtendInfo extendInfo = null;

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("additionalInfo");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoudf1udf2ud3UpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf1("abc");
        extendInfo.setUdf2("jkl");;
        extendInfo.setUdf3("xyz");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:xyz|udf1:abc|udf2:jkl|orderAlreadyCreated:true|pushDataToDynamicQr:true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoudf2ud3UpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf2("jkl");;
        extendInfo.setUdf3("xyz");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:vivek5|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:xyz|udf1:vivek1|udf2:jkl|orderAlreadyCreated:true|pushDataToDynamicQr:true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoudf1udf2udf3commentUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf1("abc");
        extendInfo.setUdf2("jkl");
        extendInfo.setUdf3("xyz");
        extendInfo.setComments("upiintenthotfix");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:upiintenthotfix|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:xyz|udf1:abc|udf2:jkl|orderAlreadyCreated:true|pushDataToDynamicQr:true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoudf1udf2commentUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf1("abc");
        extendInfo.setUdf2("jkl");
        extendInfo.setComments("upiintenthotfix");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:upiintenthotfix|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:vivek3|udf1:abc|udf2:jkl|orderAlreadyCreated:true|pushDataToDynamicQr:true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoudf1udf3commentUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf1("abc");
        extendInfo.setUdf3("xyz");
        extendInfo.setComments("upiintenthotfix");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:upiintenthotfix|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:xyz|udf1:abc|udf2:vivek2|orderAlreadyCreated:true|pushDataToDynamicQr:true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-54394")
    @Test(description = "Validate Multiple Object in Additional Info UPI Intent COP")
    public void validateMultipleObjectinAdditionalInfoudf2udf3commentUpiIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("orderAlreadyCreated:true|pushDataToDynamicQr:true");
        extendInfo.setSdkType("AIO_SDK_PG");
        extendInfo.setUdf2("jkl");
        extendInfo.setUdf3("xyz");
        extendInfo.setComments("upiintenthotfix");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setExtendInfo(extendInfo)
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"additionalInfo\":\"merchantUniqueReference:2810050501011BBRF2ET3Y18|comments:upiintenthotfix|mercUnqRef:vivek4|sdkType:AIO_SDK_PG|udf3:xyz|udf1:vivek1|udf2:jkl|orderAlreadyCreated:true|pushDataToDynamicQr:true\"");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-51768")
    @Test(description = "PG2 COP UPI PSP Txn Non Expired Flag ON")
    public void validatePG2COPUPIPSPNonExpiredFlagOn() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        String txnAmount = "100.00";
        String qrCodeId = "10NNLK";

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,qrCodeId,"" )
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();
        String esn = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        //  Assertions.assertThat(esn.length()).isEqualTo(19);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid);

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51768")
    @Test(description = "PPG2 COP UPI PSP Txn Expired Flag ON")
    public void validatePG2COPUPIPSPExpiredFlagOn() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        String txnAmount = "100.00";
        String qrCodeId = "10NNMW";

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,qrCodeId,"" )
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("payment Failure"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51768")
    @Test(description = "PG2 COP UPI PSP Txn Non Expired Flag Off")
    public void validatePG2COPUPIPSPNonExpiredFlagOff() throws Exception{
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        String txnAmount = "100.00";
        String qrCodeId = "10NNNC";

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,qrCodeId,"" )
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();
        String esn = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        //  Assertions.assertThat(esn.length()).isEqualTo(19);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid);
    }

    @Owner(CHAKSHU)
    @Feature("PGP-51768")
    @Test(description = "PG2 COP UPI PSP Txn Expired Flag Off")
    public void validatePG2COPUPIPSPExpiredFlagOff() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y;
        String mid = merchant.getId();
        String txnAmount = "100.00";
        String qrCodeId = "10NNNL";

        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,qrCodeId,"" )
                .build();
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();
        String esn = staticQrUpiPSPResponse.getBody().getExternalSerialNo();
        //  Assertions.assertThat(esn.length()).isEqualTo(19);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(HARSHITA)
    @Feature("PGP-53319")
    @Test(description = "Udf1 should be set by Theia in Pay request if udf1 is blank in v1/ptc request for Offline txn - Dynamic QR")
    public void PGP_53319_TC01() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String mid = merchantType.getId();
        String amount = "1.00";
        int posId = 192737392;

        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchantType, amount, orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1(""); //udf1(posId) is set blank in extendInfo of v1/ptc request
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "true"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId, amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("NATIVE")
                .setWebsite("retail")
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendedInfo1)
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"udf1\":\"" + posId + "\"");
    }

    @Owner(HARSHITA)
    @Feature("PGP-53319")
    @Test(description = "Udf1 should be set by Theia in COP request if udf1 is blank in v1/ptc request for Offline txn - Static QR")
    public void PGP_53319_TC02() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = MerchantType.CancelAllowed;
        String mid = merchantType.getId();
        String amount = "1.00";
        String qrCodeId = "10NLGK";
        int posId = 1234;

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
        extendInfo.setUdf1(""); //udf1(posId) is set blank in extendInfo of v1/ptc request
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}", qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "false"));
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, "SSO", user.ssoToken(), orderId, amount)
                .setChannelId("APP")
                .setOrderId(orderId)
                .setRequestType("NATIVE")
                .setWebsite("retail")
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setRiskExtendInfo(riskExtendedInfo1)
                .setExtendInfo(extendInfo)
                .setQRCodeId(qrCodeId)
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(),orderId);
        JsonPath tsJsonPath = txnStatus.executeUntilNotPending().execute().jsonPath();
        Assertions.assertThat(tsJsonPath.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS"));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"udf1\":\"" + posId + "\"");
    }
}