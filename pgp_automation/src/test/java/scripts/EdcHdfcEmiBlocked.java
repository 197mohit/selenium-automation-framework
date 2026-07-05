package scripts;

import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.QRHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("Akshat")

@Feature("PGP-28940")
public class EdcHdfcEmiBlocked extends PGPBaseTest {
    @Test(description = "Verify that HDFC EMI is not displayed/blocked in fpo of Static QR")
    public void TC_001_FPO_Static_QR() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BRAND_MERCHANT_BO_DISC_HDFC;
        String qrCodeId = QRHelper.generateQRViaWallet(merchant);

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setVersion("v2")
                .setQRCodeId(qrCodeId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        fetchQRPaymentDetails.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        Response response = fetchQRPaymentDetails.execute();
        JsonPath fqrResponse =response.jsonPath();
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.payChannelOptions.find {it.channelCode != 'HDFC'}.emiType").contains("CREDIT_CARD"))
                .as("EMI bank not found");


    }
    @Test(description = "Verify that HDFC EMI is not displayed/blocked in fpo of Payment Service EDC QR")
    public void TC_002_FPO_PaymentService_QR() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BRAND_MERCHANT_BO_DISC_HDFC;
        String txnAmount = "20.00";
        String qrCodeId = QRHelper.generateQRViaPaymentServiceEDC(merchant, txnAmount, true);

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setVersion("v2")
                .setorderId("OrderId")
                .setQRCodeId(qrCodeId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        fetchQRPaymentDetails.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        Response response = fetchQRPaymentDetails.execute();
        JsonPath fqrResponse =response.jsonPath();
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.payChannelOptions.find {it.channelCode != 'HDFC'}.emiType").contains("CREDIT_CARD"))
                .as("EMI bank not found");


    }

}
