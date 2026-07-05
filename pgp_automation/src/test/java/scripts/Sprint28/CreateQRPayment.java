package scripts.Sprint28;

import com.paytm.api.FastForward;
import com.paytm.api.SMSPrimary;
import com.paytm.api.qr.EditQR;
import com.paytm.api.qr.FetchQR;
import com.paytm.api.qr.GenerateQR;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

@Owner("Tarun")
public class CreateQRPayment extends PGPBaseTest {

    private static final String mobileNumberNotification = "9090909090";
    private static final double txnAmount = 5.0d;

    @Test(description = "Verify success txn with qr and validate sms is being sent to merchant's mobileNumber")
    public void verifySuccessFFTxnWithQR() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount);
        GenerateQR generateQR = new GenerateQR(merchantType.getId(), mobileNumberNotification);
        JsonPath jsonPath = generateQR.execute().jsonPath();
        String qrCodeId = jsonPath.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        FetchQR fetchQR = new FetchQR(qrCodeId);
        JsonPath fetchQRPath = fetchQR.execute().jsonPath();
        String orderId = fetchQRPath.getString("response.ORDER_ID");

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest.
                Builder(merchantType.getId(), orderId, String.valueOf(txnAmount))
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setMerchantContactNumber(mobileNumberNotification)
                .setQrCodeId(qrCodeId)
                .setMerchantId(merchantType.getId())
                .setMerchantGUID(fetchQRPath.getString("response.MERCHANT_GUID"))
                .build();

        JsonPath jsonPath2 = new FastForward(fastForwardAppRequest).execute().jsonPath();
        Assertions.assertThat(jsonPath2.getString("body.resultInfo.resultStatus")).as("Fast forward API is not working")
                .isEqualTo("TXN_SUCCESS");

        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        smsPrimary.executeUntilGetResponse();

        //sms is being sent to correct number or not
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo")).as("SMS is being sent to wrong number").isEqualTo(mobileNumberNotification);

    }

    @Test(description = "Verify number is changed and then cached qr info is used for txn , then number is changed in repsonse of getqrinfo")
    public void editQrInfoAndgetQRInfo()
        {
            Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
            String updatedMobileNumberNotification = "9999888877";
            GenerateQR generateQR = new GenerateQR(merchantType.getId(),mobileNumberNotification);
            String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
            JsonPath generateJson = JsonPath.given(resp);
            String qrCodeId = generateJson.getString("response[0].qrCodeId");

            EditQR editQR = new EditQR(qrCodeId, updatedMobileNumberNotification);
            Assertions.assertThat(editQR.execute().jsonPath().getString("statusCode")).as("Error in edit Qr API")
                    .isEqualTo("200");
            FetchQR fetchQR = new FetchQR(qrCodeId);
            Assertions.assertThat(fetchQR.execute().jsonPath().getString("response.merchantContactNo")).isEqualTo(updatedMobileNumberNotification);

        }

    @Test(description = "Verify sms is being sent to updated mobile number via pgproxy/queue-handler/comm gateway")
    public void editQrInfoAndfastForwardTxn() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user  = userManager.getForWrite(Label.BASIC);
        String updatedMobileNumberNotification = "9999888877";
        WalletHelpers.modifyBalance(user, txnAmount);
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),mobileNumberNotification);
        String qrCodeId = generateQR.execute().jsonPath().getString("response.qrCodeId").replaceAll("\\p{P}", "");

        EditQR editQR = new EditQR(qrCodeId, updatedMobileNumberNotification);
        editQR.execute().jsonPath();
        Assertions.assertThat(editQR.execute().jsonPath().getString("statusCode")).as("Error in edit Qr API")
                .isEqualTo("200");

        FetchQR fetchQR = new FetchQR(qrCodeId);
        JsonPath fetchQRPath =  fetchQR.execute().jsonPath();

        String orderId = CommonHelpers.generateOrderId();

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest.
                Builder(merchantType.getId(), orderId, String.valueOf(txnAmount))
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setTxnAmount(String.valueOf(txnAmount))
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setMerchantContactNumber(updatedMobileNumberNotification)
                .setQrCodeId(qrCodeId)
                .setMerchantId(merchantType.getId())
                .setMerchantGUID(fetchQRPath.getString("response.MERCHANT_GUID"))
                .build();

        JsonPath jsonPath = new FastForward(fastForwardAppRequest).execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).as("Fast forward API is not working")
                .isEqualTo("TXN_SUCCESS");

        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        smsPrimary.executeUntilGetResponse();

        //sms is being sent to correct number or not
        Assertions.assertThat(smsPrimary.execute().jsonPath().getString("mobileNo")).as("SMS is being sent to wrong number").isEqualTo(updatedMobileNumberNotification);

    }

    }

