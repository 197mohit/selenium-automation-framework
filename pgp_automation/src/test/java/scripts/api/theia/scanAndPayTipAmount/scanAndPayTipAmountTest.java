package scripts.api.theia.scanAndPayTipAmount;

import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TransactionStatusV1API;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TipDetails;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class scanAndPayTipAmountTest extends PGPBaseTest {
    private final String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    private final String riskExtendedInfo1 = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:Dynamic043|mode:qrBackEnd|wifi:%3Cunknown+ssid%3E|mode:qrBackEnd|userLBSLatitude:|userLBSLongitude:|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:fcdc63f9b1c2b2b4|appVersion:9.19.0|versionCode:710691|osType:Android|phoneModel:Redmi+Note+5|IMEI:869822033025526|deviceManufacturer:Xiaomi|deviceLanguage:en|timeZone:GMT+05:30|routerMac:02:00:00:00:00:00|clientIp:192.168.18.10|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:fcdc63f9b1c2b2b4|screenResolution:1080x2160|isGalleryScan:false";
    private final String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|orderAlreadyCreated:{orderAlreadyCreated}";

    public static String generateQRViaWallet(Constants.MerchantType merchantType)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"");
    //    JsonPath generateJson = generateQR.execute().jsonPath();
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        System.out.println(resp);
        JsonPath generateJson = JsonPath.given(resp);

        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }

    @Owner(Constants.Owner.VAIBHAV)
    @Feature("PGP-47627")
    @Test(description = "Validate tipDetails must be returned for UPI paymode in FQR & PTC API response")
    public void validateTipDetails() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        com.paytm.dto.processTransactionV1.TxnAmount txnAmount = new com.paytm.dto.processTransactionV1.TxnAmount();
        txnAmount.setValue("100");
        txnAmount.setCurrency("INR");
        Constants.MerchantType merchant = Constants.MerchantType.TIP_AMOUNT;
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
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.tipDetails.tipAmounts")).contains("[amount:5.0, mostTipped:false]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.tipDetails.tipAmounts")).contains("[amount:10.0, mostTipped:true]");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.tipDetails.tipAmounts")).contains("[amount:20.0, mostTipped:false]");
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", mid).replace("{orderAlreadyCreated}", "false"));

        com.paytm.dto.processTransactionV1.TipAmount tipAmount= new com.paytm.dto.processTransactionV1.TipAmount();
        tipAmount.setValue("10");
        tipAmount.setCurrency("INR");

        TipDetails tipDetails = new TipDetails();
        tipDetails.setTipAmount(tipAmount);

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
                .setTipDetails(tipDetails)
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response v1result = processTransactionV1.execute();
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.GATEWAYNAME")).contains("PPBEX");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.PAYMENTMODE")).contains("UPI");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.STATUS")).contains("TXN_SUCCESS");
        Assertions.assertThat(v1result.jsonPath().getString("body.txnInfo.MID")).contains(merchant.getId());
        Assertions.assertThat(v1result.jsonPath().getString("body.tipDetails")).contains("[tipAmount:[currency:INR, value:10.00]");


    }

    @Owner(Constants.Owner.VAIBHAV)
    @Feature("PGP-47627")
    @Test(description = "Validate tipDetails must be returned in FPO API response")
    public void validateTipDetailsFQR() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.TIP_AMOUNT;
        String orderId = CommonHelpers.generateOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setOfflineFlow("true")
                .setToken(user.ssoToken())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), orderId, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentResponse.getString("body.tipDetails.tipAmounts")).contains("[amount:5.0, mostTipped:false]");
        Assertions.assertThat(fetchPaymentResponse.getString("body.tipDetails.tipAmounts")).contains("[amount:10.0, mostTipped:true]");
        Assertions.assertThat(fetchPaymentResponse.getString("body.tipDetails.tipAmounts")).contains("[amount:20.0, mostTipped:false]");


    }



    }
