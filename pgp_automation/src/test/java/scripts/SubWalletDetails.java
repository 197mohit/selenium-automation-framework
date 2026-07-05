package scripts;

import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.pages.CheckoutJsCheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

@Owner("Akshat")
@Feature("PGP-32831")
public class SubWalletDetails extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;
    }
    @Test(description = "Verify that only Paytm Wallet & Gift Voucher is displayed in fpov2 when SUBWALLET_SEGREGATION_ENABLED - N ")
    public void TC_001_paytmWallet_giftVoucher_displayedInFPO() throws Exception {

        Double txnAmount = 1.0;
        User user = userManager.getForRead(Label.PRIORITY);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find {it.paymentMode == 'BALANCE'}.payChannelOptions.balanceInfo.subWalletDetails.displayName"))
                .as("sub wallet mismatch")
               .isEqualTo("[[Paytm Wallet, Gift Voucher]]");

    }

    @Test(description = "Verify that Food Wallet is not displayed in fpov2 when SUBWALLET_SEGREGATION_ENABLED - Y and food category is not enabled ")
    public void TC_002_foodWallet_notDisplayed_inFPO() throws Exception {

        Double txnAmount = 1.0;
        User user = userManager.getForRead(Label.PRIORITY);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find {it.paymentMode == 'BALANCE'}.payChannelOptions.balanceInfo.subWalletDetails.displayName"))
                .as("sub wallet mismatch")
               .isEqualTo("[[Paytm Wallet, Gift Wallet, Remittance Wallet, Loyalty Points, Gift Voucher]]");
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find {it.paymentMode == 'BALANCE'}.payChannelOptions.balanceInfo.subWalletDetails.displayName"))
                .as("sub wallet mismatch")
                .isNotEqualTo("[[Food Wallet]]");
    }

    @Test(description = "Verify that all Sub Wallet are displayed in fpov2 when SUBWALLET_SEGREGATION_ENABLED - Y")
    public void TC_003_subbWallets_Displayed_inFPO() throws Exception {

        Double txnAmount = 1.0;
        User user = userManager.getForRead(Label.PRIORITY);
        Constants.MerchantType merchant = Constants.MerchantType.FOOD_WALLET_PAYTMCC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes.find {it.paymentMode == 'BALANCE'}.payChannelOptions.balanceInfo.subWalletDetails.displayName"))
                .as("sub wallet mismatch")
                .isEqualTo("[[Paytm Wallet, Food Wallet, Gift Wallet, Remittance Wallet, Loyalty Points, Gift Voucher]]");
    }

}