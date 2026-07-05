package scripts.api.theia.chatpayment;

import com.paytm.api.FastForward;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.VIKASH_VERMA;
import static scripts.api.Wallet.PG2Wallet.generateQRViaWallet;

public class chatpayment extends PGPBaseTest {

    @Owner(VIKASH_VERMA)
    @Feature("PGP-48130")
    @Test(description = "Block the transaction when the offlineappmode is chat for PTC")
    public void BlockchatpaymentPTC() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Attribute_key_mid2;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails
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
        com.paytm.dto.processTransactionV1.ExtendInfo extendInfo = new com.paytm.dto.processTransactionV1.ExtendInfo();
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|offlineAppMode: chat|orderAlreadyCreated:{orderAlreadyCreated}";
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", merchant.getId()).replace("{orderAlreadyCreated}", "false"));
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),  "SSO",user.ssoToken(),orderId ,"50")
                .setPaymentMode("DEBIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .setExtendInfo(extendInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
    }
    @Owner(VIKASH_VERMA)
    @Feature("PGP-48130")
    @Test(description = "Block the transaction when the offlineappmode is chat for Fast forward")
    public void BlockchatpaymentFastforward() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.Attribute_key_mid2;
        String qrCodeId = generateQRViaWallet(merchant);

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
        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(merchant.getId(), orderId, "50")
                .setofflineAppMode()
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .setQrCodeId(qrCodeId)
                .setOrderId(orderId)
                .setOrderAlreadyCreated("false")
                .build();
        FastForward fastForward = new FastForward(fastForwardAppRequest);
        Response fastForwardResponse = fastForward.execute();
        fastForwardResponse.prettyPrint();
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("This merchant doesn't accept payment via Chat/MobileNo. Don't worry money has not deducted. Please try scanning a Merchant QR Code");
        Assertions.assertThat(fastForwardResponse.jsonPath().getString("body.resultInfo.resultStatus")).contains("F");
    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-48130")
    @Test(description = "Block the transaction when the offlineappmode is newchatmigrate for PTC")
    public void BlockchatpaymentPTCregexcheck() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Attribute_key_mid2;
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails
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
        com.paytm.dto.processTransactionV1.ExtendInfo extendInfo = new com.paytm.dto.processTransactionV1.ExtendInfo();
        String additionalinfo = "payeeType:MERCHANT|service:P2M|posId:Dynamic042|mode:QR_CODE|offlinePostConvenience:false|mappingId:{{mid}}|pgEnabled:true|qrCodeId:{{qrCodeId}}|merchantTransId:{{orderId}}|merchantVerified:true|orderAlreadyCreated:{orderAlreadyCreated}|orderQr:true|REQUEST_TYPE:UPI_QR_CODE|EXPIRY_DATE:1643621579000|MERCHANT_NAME:Dynamic043|NAME:Dynamic043|MOBILE_NO:1234567890|TXN_AMOUNT:10|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:2abbb055-3e6e-491b-b28a-ab864e1dd045|ORDER_ID:{{orderId}}|CHANNEL_ID:QRCODE|qr_code_id:{{qrCodeId}}|RECENTS_NAME:merchante|comment:|REQUEST_TYPE:ADD_MONEY|offlineAppMode: newchatmigrate|orderAlreadyCreated:{orderAlreadyCreated}";
        extendInfo.setAdditionalInfo(additionalinfo.replace("{{qrCodeId}}",qrCodeId).replace("{{orderId}}", orderId).replace("{{mid}}", merchant.getId()).replace("{orderAlreadyCreated}", "false"));
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" +paymentDTO.DEBIT_CARD_NUMBER+"|"+paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth()+ paymentDTO.getExpYear();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(),  "SSO",user.ssoToken(),orderId ,"50")
                .setPaymentMode("DEBIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(cardInfo)
                .setExtendInfo(extendInfo)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
    }
}
