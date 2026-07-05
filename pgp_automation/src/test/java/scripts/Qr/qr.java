package scripts.Qr;

import com.paytm.api.PaymentService;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.processTransactionV1.TxnAmount;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.base.test.PGPBaseTest.userManager;
import static io.restassured.RestAssured.given;

public class qr extends PGPBaseTest{

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-42679")
    @Test(description = "Verify isAddMoneyPayOptionsAvailable:true is returned in FetchQRPaymentDetails and FPO response for Addnpay scenario when user have postpaid and merchant doesn't have postpaid on it.")
    public void Verify_isAddMoneyPayOptionsAvailable_true_user_postpaid_merchant_nonpostpaid() throws Exception {
        User user = new User("8006006993","paytm@123");
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay_Retry;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
    //  JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setTokenType("SSO").setToken(user.ssoToken())
                .setsupportedPayModesForAddNPay("PPBL,UPI")
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        Response qrresponse = qr.execute();
        qrresponse.jsonPath().getString("body.paymentOptions.isAddMoneyPayOptionsAvailable").equals(true);
        FetchPaymentOptionsDTO fetchPaymentOption = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchant.getId())
                .setsupportedPayModesForAddNPay("PPBL,UPI")
                .build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(merchant.getId(),fetchPaymentOption);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption1.execute().jsonPath();
        fetchPaymentOptionsJson.getString("body.isAddMoneyPayOptionsAvailable").equals(true);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-42679")
    @Test(description = "Verify isAddMoneyPayOptionsAvailable:true is returned in FetchQRPaymentDetails and FPO response for Addnpay scenario when user does not have postpaid and merchant have postpaid on it.")
    public void Verify_isAddMoneyPayOptionsAvailable_true_user_nonpostpaid_merchant_postpaid() throws Exception {
        User user = new User("9813981530","paytm@123");
        Constants.MerchantType merchant = Constants.MerchantType.COBRANDED_DEPRIORITISE_DC;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
    //  JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrid = generateJson.getString("response[0].qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrid).setTokenType("SSO").setToken(user.ssoToken())
                .setsupportedPayModesForAddNPay("PPBL,UPI")
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        Response qrresponse = qr.execute();
        qrresponse.jsonPath().getString("body.paymentOptions.isAddMoneyPayOptionsAvailable").equals(true);
        FetchPaymentOptionsDTO fetchPaymentOption = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchant.getId())
                .setsupportedPayModesForAddNPay("PPBL,UPI")
                .build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(merchant.getId(),fetchPaymentOption);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption1.execute().jsonPath();
        fetchPaymentOptionsJson.getString("body.isAddMoneyPayOptionsAvailable").equals(true);
    }
    @Owner(VIDHI)
    @Feature("PGP-43930")
    @Test(description = "Verify bffLayerEnabled field as TRUE in fetchQRPaymentDetails API response when MID is present in FF4J theia.bff.layer.enabled ")
    public void verify_bffLayerEnabled_in_FetchQRPaymentetails_FF4J_ON() throws Exception {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType merchant = Constants.MerchantType.BFF_LAYERED_FPO;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.bffLayerEnabled")).isEqualTo("true");
    }
    @Owner(VIDHI)
    @Feature("PGP-43930")
    @Test(description = "Verify bffLayerEnabled field as TRUE in fetchQRPaymentDetails API response when MID is present in FF4J theia.bff.layer.enabled ")
    public void verify_bffLayerEnabled_in_FPO_FF4J_ON() throws Exception {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType merchant = Constants.MerchantType.BFF_LAYERED_FPO;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchPaymentOptionsDTO fetchPaymentOption = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchant.getId())
                .build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(merchant.getId(),fetchPaymentOption);
        JsonPath fpoResponse = fetchPaymentOption1.execute().jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.bffLayerEnabled")).isEqualTo("true");
    }
    @Owner(VIDHI)
    @Feature("PGP-43930")
    @Test(description = "Verify bffLayerEnabled field as FALSE in fetchQRPaymentDetails API response when MID is not present in FF4J theia.bff.layer.enabled ")
    public void verify_bffLayerEnabled_in_FetchQRPaymentetails_FF4J_OFF() throws Exception {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.bffLayerEnabled")).isEqualTo("false");
    }
    @Owner(VIDHI)
    @Feature("PGP-43930")
    @Test(description = "Verify bffLayerEnabled field as FALSE in fetchQRPaymentDetails API response when MID is not present in FF4J theia.bff.layer.enabled ")
    public void verify_bffLayerEnabled_in_FPO_FF4J_OFF() throws Exception {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchPaymentOptionsDTO fetchPaymentOption = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchant.getId())
                .build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(merchant.getId(),fetchPaymentOption);
        JsonPath fpoResponse = fetchPaymentOption1.execute().jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.bffLayerEnabled")).isEqualTo("false");
    }
    private final String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|orderAlreadyCreated:{orderAlreadyCreated}";

    private final String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
//    @Owner(MAYURI)
//    @Feature("PGP-45675")
//    @Test(description = "Validate REQUEST_TYPE in riskExtendInfo for Dynamic QR COTP txn",enabled = false)
    public void validateREQUEST_TYPEinRiskExtendInfo_DynamicQRCOTP() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = Constants.MerchantType.CancelAllowed.getId();
        String amount = "10";
//        WalletHelpers.modifyBalance(user, Double.parseDouble(amount));
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(Constants.MerchantType.CancelAllowed, amount, orderId);
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

        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("PPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getTXNID()).contains("0000");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER", "REQUEST");
        Assertions.assertThat(logs).contains("REQUEST_TYPE:ADD_MONEY");
    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC queryParams in risk extend info section of Payment cashier pay request of Static QR flow when device is IOS and ff4j flag=theia.send.query.params.in.risk.ext.info")
    public void verify_QueryParams_RiskExtendInfo_StaticQR_IOS() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String osVersion= "16.4.1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="D7C07074-8821-4CD6-8AF8-93EA9FE47E83:1699603252:03:036";
        String xSimSubId="";

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", "false"));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,osVersion,"",lat,longitude,networkType,version,"",xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");

        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "ACQUIRING_CREATE_ORDER_AND_PAY";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"deviceIdentifier\":\""+deviceIdentifier+"\"");
        Assertions.assertThat(logs).contains("\"osVersion\":\""+osVersion+"\"");
        Assertions.assertThat(logs).contains("\"lat\":\""+lat+"\"");
        Assertions.assertThat(logs).contains("\"long\":\""+longitude+"\"");
        Assertions.assertThat(logs).contains("\"networkType\":\""+networkType+"\"");
        Assertions.assertThat(logs).contains("\"version\":\""+version+"\"");

    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC queryParams in risk extend info section of Payment cashier pay request of Static QR flow when device is ANDROID and ff4j flag=theia.send.query.params.in.risk.ext.info")
    public void verify_QueryParams_RiskExtendInfo_StaticQR_Android() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String simSubscriptionId= "1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String playStore="true";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="0d4e1033395689c6:1699962368647:2:7f";
        String xSimSubId="1";

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", "false"));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,"",simSubscriptionId,lat,longitude,networkType,version,playStore,xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");

        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "ACQUIRING_CREATE_ORDER_AND_PAY";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"deviceIdentifier\":\""+deviceIdentifier+"\"");
        Assertions.assertThat(logs).contains("\"simSubscriptionId\":\""+simSubscriptionId+"\"");
        Assertions.assertThat(logs).contains("\"lat\":\""+lat+"\"");
        Assertions.assertThat(logs).contains("\"long\":\""+longitude+"\"");
        Assertions.assertThat(logs).contains("\"networkType\":\""+networkType+"\"");
        Assertions.assertThat(logs).contains("\"version\":\""+version+"\"");
        Assertions.assertThat(logs).contains("\"playStore\":\""+playStore+"\"");

    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC queryParams in risk extend info section of Payment cashier pay request of Dynamic QR flow when device is IOS and ff4j flag=theia.send.query.params.in.risk.ext.info")
    public void verify_QueryParams_RiskExtendInfo_DynamicQR_IOS() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String osVersion= "16.4.1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="D7C07074-8821-4CD6-8AF8-93EA9FE47E83:1699603252:03:036";
        String xSimSubId="";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType, "10", orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", fetchQRResponse.jsonPath().getString("body.qrInfo.response.orderAlreadyCreated")));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,osVersion,"",lat,longitude,networkType,version,"",xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");

        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"deviceIdentifier\":\""+deviceIdentifier+"\"");
        Assertions.assertThat(logs).contains("\"osVersion\":\""+osVersion+"\"");
        Assertions.assertThat(logs).contains("\"lat\":\""+lat+"\"");
        Assertions.assertThat(logs).contains("\"long\":\""+longitude+"\"");
        Assertions.assertThat(logs).contains("\"networkType\":\""+networkType+"\"");
        Assertions.assertThat(logs).contains("\"version\":\""+version+"\"");

    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC queryParams in risk extend info section of Payment cashier pay request of Dynamic QR flow when device is ANDROID and ff4j flag=theia.send.query.params.in.risk.ext.info")
    public void verify_QueryParams_RiskExtendInfo_DynamicQR_Android() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String simSubscriptionId= "1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String playStore="true";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="0d4e1033395689c6:1699962368647:2:7f";
        String xSimSubId="1";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType, "10", orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", fetchQRResponse.jsonPath().getString("body.qrInfo.response.orderAlreadyCreated")));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,"",simSubscriptionId,lat,longitude,networkType,version,playStore,xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");
        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "ACQUIRING_PAY_ORDER";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"deviceIdentifier\":\""+deviceIdentifier+"\"");
        Assertions.assertThat(logs).contains("\"simSubscriptionId\":\""+simSubscriptionId+"\"");
        Assertions.assertThat(logs).contains("\"lat\":\""+lat+"\"");
        Assertions.assertThat(logs).contains("\"long\":\""+longitude+"\"");
        Assertions.assertThat(logs).contains("\"networkType\":\""+networkType+"\"");
        Assertions.assertThat(logs).contains("\"version\":\""+version+"\"");
        Assertions.assertThat(logs).contains("\"playStore\":\""+playStore+"\"");

    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC headers in payment request when ff4j - appendHeadersInQueryParamRedis is ON | Static QR on IOS device")
    public void verify_headersInPtc_Risk_PaymentRequestStaticQR_IOS() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String osVersion= "16.4.1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="D7C07074-8821-4CD6-8AF8-93EA9FE47E83:1699603252:03:036";
        String xSimSubId="";

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", "false"));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,osVersion,"",lat,longitude,networkType,version,"",xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");

        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "Payment Request| BankCode:PPBEX";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("x-integrity="+ xIntegrity);
        Assertions.assertThat(logs).contains("x-deb-status="+ xDebStatus);
        Assertions.assertThat(logs).contains("x-app-rid="+ xAppRid);
    }
    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC headers in payment request when ff4j - appendHeadersInQueryParamRedis is ON | Static QR on Android device")
    public void verify_headersInPtc_Risk_PaymentRequestStaticQR_Android() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String simSubscriptionId= "1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String playStore="true";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="0d4e1033395689c6:1699962368647:2:7f";
        String xSimSubId="1";

        OrderDTO orderDTO = QRHelper.generateQRAndFetchQRPaymentDetailsStatic(merchantType, "2", user);
        String qrCodeId = orderDTO.getTxnId();
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        String orderId = fetchQRResponse.then().extract().jsonPath().getString("body.paymentOptions.orderId");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", "false"));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,"",simSubscriptionId,lat,longitude,networkType,version,playStore,xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");

        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "Payment Request| BankCode:PPBEX";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("x-integrity="+ xIntegrity);
        Assertions.assertThat(logs).contains("x-deb-status="+ xDebStatus);
        Assertions.assertThat(logs).contains("x-app-rid="+ xAppRid);
        Assertions.assertThat(logs).contains("x-sim-sub-id="+ xSimSubId);
    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC headers in payment request when ff4j - appendHeadersInQueryParamRedis is ON | Dynamic QR on IOS device")
    public void verify_headersInPtc_Risk_PaymentRequestDynamicQR_IOS() throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String osVersion= "16.4.1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="D7C07074-8821-4CD6-8AF8-93EA9FE47E83:1699603252:03:036";
        String xSimSubId="";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType, "10", orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", fetchQRResponse.jsonPath().getString("body.qrInfo.response.orderAlreadyCreated")));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,osVersion,"",lat,longitude,networkType,version,"",xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");
        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "Payment Request| BankCode:PPBEX";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("x-integrity="+ xIntegrity);
        Assertions.assertThat(logs).contains("x-deb-status="+ xDebStatus);
        Assertions.assertThat(logs).contains("x-app-rid="+ xAppRid);

    }

    @Owner(VIDHI)
    @Feature("PGP-50359")
    @Test(description = "Verify the V1/PTC headers in payment request when ff4j - appendHeadersInQueryParamRedis is ON | Dynamic QR on Android device")
    public void verify_headersInPtc_Risk_PaymentRequestDynamicQR_Android() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchant|comment:|REQUEST_TYPE:UPI_QR_CODE|orderAlreadyCreated:{orderAlreadyCreated}";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        String deviceIdentifier= "Apple-iPhone-D7C07074-8821-4CD6-8AF8-93EA9FE47E83";
        String simSubscriptionId= "1";
        Double lat=21.07098388671875;
        Double longitude=79.81671045332916;
        String networkType="WiFi";
        String version="10.36.0";
        String playStore="true";
        String xIntegrity="true";
        String xDebStatus="true";
        String xAppRid="0d4e1033395689c6:1699962368647:2:7f";
        String xSimSubId="1";
        String orderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchantType, "10", orderId);
        String qrCodeId = paymentService.execute().then()
                .statusCode(200)
                .extract().jsonPath()
                .getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}",merchantType.getId()).replace("{orderAlreadyCreated}", fetchQRResponse.jsonPath().getString("body.qrInfo.response.orderAlreadyCreated")));
        TxnAmount txnAmount=new TxnAmount();
        txnAmount.setValue("10");
        txnAmount.setCurrency("INR");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType, "SSO" , user.ssoToken() )
                .setChannelId("APP")
                .setOrderId(orderId)
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
                .setCreditBlock(creditblock)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,deviceIdentifier,"",simSubscriptionId,lat,longitude,networkType,version,playStore,xIntegrity,xDebStatus,xAppRid,xSimSubId);
        processTransactionV1.getRequestSpecBuilder().addHeader("source","OFFLINE");
        Response response = processTransactionV1.execute();
        System.out.println(response);
        String grepcmd = "Payment Request| BankCode:PPBEX";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("x-integrity="+ xIntegrity);
        Assertions.assertThat(logs).contains("x-deb-status="+ xDebStatus);
        Assertions.assertThat(logs).contains("x-app-rid="+ xAppRid);
        Assertions.assertThat(logs).contains("x-sim-sub-id="+ xSimSubId);

    }


}
