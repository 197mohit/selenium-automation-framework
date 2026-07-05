package scripts.api.Wallet;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
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
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.Date;
import static com.paytm.appconstants.Constants.Owner.*;
import static io.restassured.RestAssured.given;


public class WalletReactivation extends PGPBaseTest
{

    String deviceType = "android";
    String appVersion = "33.35.0";

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for active wallet user android fpo")
    public void activeWallet(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        MerchantType merchant = MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for active wallet user ios fpo")
    public void activeWallet_ios(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="ios";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        MerchantType merchant = MerchantType.UPIPUSHPG2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is Y and theia.wallet.reactivation.enabled is ON")
    public void inactiveWallet_Pref_Y_reactivationFlag_ON(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CVV_LESS_MID).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is Y and theia.wallet.reactivation.enabled is ON")
    public void inactiveWallet_Pref_Y_reactivationFlag_ON_IOS(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="ios";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.CVV_LESS_MID).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is Y and theia.wallet.reactivation.enabled is OFF")
    public void inactiveWallet_Pref_Y_reactivationFlag_OFF(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.WALLET_UPI_MID).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is Y and theia.wallet.reactivation.enabled is OFF")
    public void inactiveWallet_Pref_Y_reactivationFlag_OFF_IOS(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.WALLET_UPI_MID).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is OFF")
    public void inactiveWallet_Pref_N_reactivationFlag_OFF(@Optional("false") Boolean isNativePlus) throws Exception
    {

        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is OFF")
    public void inactiveWallet_Pref_N_reactivationFlag_OFF_IOS(@Optional("false") Boolean isNativePlus) throws Exception
    {

        deviceType="ios";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON but wallet balance is 0")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_balanceZero_android(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLETZEROBAL);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON but wallet balance is 0")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_balanceZero_ios(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="ios";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLETZEROBAL);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON but wallet balance is >5000")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_balanceSixThousand_Android(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLETZEROBAL);
        WalletHelpers.modifyBalance(user,6000.00);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }


    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for frozen wallet user ")
    public void frozenWallet_Pref_N_reactivationFlag_ON_Android(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.FROZENWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }


    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for frozen wallet user ")
    public void frozenWallet_Pref_N_reactivationFlag_ON_IOS(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="ios";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.FROZENWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation comes come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_ANDROID(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation comes come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_IOS(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="ios";
        appVersion="33.35.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));
//        Assertions.assertThat(fetchPaymentOptionsJson.getString(("body.superCashOffers.supercashPayModes.find {it.paymode == 'Paytm Balance'}.isActive")).contains("\"userWalletReactivation\": true"));
        System.out.println((fetchPaymentOptionsJson.getString(("body.merchantPayOptions.paymentModes.find {it.paymode == 'Paytm Balance'}.isActive"))));
    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON but app version is less than in ff4j")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_ANDROID_appversionless(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="android";
        appVersion="10.10.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-50394")
    @Parameters("theme")
    @Test(description = "Validate user wallet reactivation doesn't come for inactive wallet user when WALLET_REACTIVATION_EXEMPTION is N and theia.wallet.reactivation.enabled is ON but ios fpo version is less")
    public void inactiveWallet_Pref_N_reactivationFlag_ON_IOS_Appversionless(@Optional("false") Boolean isNativePlus) throws Exception
    {
        deviceType="ios";
        appVersion="10.10.0";
        User user = userManager.getForWrite(Label.INACTIVEWALLET);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPIPUSHPG2).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.orderFromBody();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                orderID, deviceType,fetchPaymentOptionsDTO,appVersion);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("\"userWalletReactivation\": true")));
    }


}