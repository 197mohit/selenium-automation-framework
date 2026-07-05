package scripts;


import com.google.inject.internal.cglib.core.$Constants;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.QRHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Akshat")
public class PriorityParamUPI extends PGPBaseTest {

    @Test(description = "Verify that priority is displayed in FPO for UPI when no saved VPA on user")
    public void TC_001_No_Saved_VPA() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Double txnAmount = 1.0;

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.priority"))
                .as("priority mismatch")
                .isNotEmpty();

    }

    @Test(description = "Verify that priority is displayed in FPO for UPI when VPA are saved on user")
    public void TC_002_Saved_VPA() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Double txnAmount = 2.0;

        User user = userManager.getForRead(Label.SAVEDVPA);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.priority"))
                .as("priority mismatch")
                .isNotEmpty();

    }

    @Test(description = "Verify that priority is displayed in FPO for UPI for non loggedIn user")
    public void TC_003_Non_LoggedIn_Flow() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Double txnAmount = 3.0;

        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.priority"))
                .as("priority mismatch")
                .isNotEmpty();

    }

    @Test(description = "Verify that priority is displayed in FPO for UPI when VPA are saved on user for v1 api")
    public void TC_004_Saved_VPA() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Double txnAmount = 4.0;

        User user = userManager.getForRead(Label.SAVEDVPA);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.priority"))
                .as("priority mismatch")
                .isNotEmpty();

    }

    @Test(description = "Verify that priority is displayed in fetchQRPaymentDetails for UPI when no VPA saved on user")
    public void TC_005_fetch_QR_details() throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
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
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.priority"))
                .as("priority mismatch")
                .isNotEmpty();

    }

    @Test(description = "Verify that priority is displayed in FPO for UPI when no saved VPA on user")
    public void TC_006_No_Saved_VPA_MID_SSO() throws Exception {

        Double txnAmount = 6.0;
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setMid(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setAmount((txnAmount))
                .setGenerateOrderId("true")
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), fetchPaymentOptionsDTO.getBody().getGenerateOrderId(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.priority"))
                 .as("priority mismatch")
                 .isNotEmpty();

    }
}