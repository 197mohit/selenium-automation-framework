package scripts.api.FPO;

import com.paytm.api.nativeAPI.DynamicFPO;
import com.paytm.api.nativeAPI.DynamicFQR;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class walletRemovedInFPO extends PGPBaseTest
{

    /*
    * FPO Cases
    */
    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54256")
    @Test(description = "Verify wallet comes in fpo resp when wallet balance is <1 when theia.skip.wallet.on.debitOrOverallFreeze is off")
    public void walletRemovedfromFpo_ZerotoOneBalance_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.50);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .contains("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54256")
    @Test(description = "Verify wallet doesn't come in fpo resp when wallet balance is  <1 when theia.skip.wallet.on.debitOrOverallFreeze flag is on")
    public void walletRemovedfromFpo_ZerotoOneBalance_FF4JOn() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.50);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54256")
    @Test(description = "Verify wallet comes in fpo resp when wallet balance is >1 when theia.skip.wallet.on.debitOrOverallFreeze is on")
    public void walletRemovedfromFpo_SufficientBalance_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,5.00);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .contains("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet doesn't come in fpo resp when wallet is in debit freeze state when theia.skip.wallet.on.debitOrOverallFreeze flag is on")
    public void walletRemovedfromFpo_DebitFreeze_FF4JOn() throws Exception
    {
        User user = userManager.getForWrite(Label.FROZENWALLET);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet doesn't come in fpo resp when wallet is in overall freeze when theia.skip.wallet.on.debitOrOverallFreeze flag is on")
    public void walletRemovedfromFpo_OverallFreeze_FF4JOn() throws Exception
    {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet comes in fpo resp when wallet is in debit freeze state when theia.skip.wallet.on.debitOrOverallFreeze flag is off")
    public void walletRemovedfromFpo_DebitFreeze_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.FROZENWALLET);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .contains("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet comes in fpo resp when wallet is in overall freeze when theia.skip.wallet.on.debitOrOverallFreeze flag is off")
    public void walletRemovedfromFpo_OverallFreeze_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .contains("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet comes in fpo resp when wallet is in credit freeze state when theia.skip.wallet.on.debitOrOverallFreeze flag is off")
    public void walletRemovedfromFpo_CreditFreeze_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.INACTIVEWALLETZEROBAL);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid ).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.displayName"))
                .contains("Paytm Balance");
    }

    /*
    *FQR Cases
     */
    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54256")
    @Test(description = "Verify wallet comes in fqr resp when wallet balance is <1 when theia.skip.wallet.on.debitOrOverallFreeze is off")
    public void walletRemovedfromFqr_ZerotoOneBalance_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.50);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName")
                .contains("Paytm Balance"));

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54256")
    @Test(description = "Verify wallet doesn't come in fqr resp when wallet balance is  <1 when theia.skip.wallet.on.debitOrOverallFreeze flag is on")
    public void walletRemovedfromFqr_ZerotoOneBalance_FF4JOn() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,0.50);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54256")
    @Test(description = "Verify wallet comes in fqr resp when wallet balance is >1 when theia.skip.wallet.on.debitOrOverallFreeze is on")
    public void walletRemovedfromFqr_SufficientBalance_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user,5.00);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName")
                .contains("Paytm Balance"));

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet doesn't come in fqr resp when wallet is in debit freeze state when theia.skip.wallet.on.debitOrOverallFreeze flag is on")
    public void walletRemovedfromFqr_DebitFreeze_FF4JOn() throws Exception
    {
        User user = userManager.getForWrite(Label.FROZENWALLET);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet doesn't come in fqr resp when wallet is in overall freeze when theia.skip.wallet.on.debitOrOverallFreeze flag is on")
    public void walletRemovedfromFqr_OverallFreeze_FF4JOn() throws Exception
    {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType mid = Constants.MerchantType.Static_True_Recent_True;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName"))
                .doesNotContain("Paytm Balance");

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet comes in fqr resp when wallet is in debit freeze state when theia.skip.wallet.on.debitOrOverallFreeze flag is off")
    public void walletRemovedfromFqr_DebitFreeze_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName")
                .contains("Paytm Balance"));

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet comes in fqr resp when wallet is in overall freeze when theia.skip.wallet.on.debitOrOverallFreeze flag is off")
    public void walletRemovedfromFqr_OverallFreeze_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.STORECASH);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        GenerateQR generateQR = new GenerateQR( mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName")
                .contains("Paytm Balance"));

    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-54255")
    @Test(description = "Verify wallet comes in fqr resp when wallet is in credit freeze state when theia.skip.wallet.on.debitOrOverallFreeze flag is off")
    public void walletRemovedfromFqr_CreditFreeze_FF4JOff() throws Exception
    {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType mid = Constants.MerchantType.WALLET_UPI_MID;
        GenerateQR generateQR = new GenerateQR(mid.getId(), "");
        String resp = generateQR.execute().getBody().asString().replace("\\", "").replace("\"{","{").replace("}\"","}");
        JsonPath generateJson = JsonPath.given(resp);
        String qrCodeId = generateJson.getString("response[0].qrCodeId");

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mid.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.displayName")
                .contains("Paytm Balance"));

    }

}