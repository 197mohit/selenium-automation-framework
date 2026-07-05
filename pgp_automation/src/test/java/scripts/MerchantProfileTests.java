package scripts;

import com.paytm.CreateToken;
import com.paytm.api.MappingService.MerchantProfile;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import com.paytm.utils.merchant.util.PGPUtil;

import java.util.UUID;


public class MerchantProfileTests extends PGPBaseTest {

    @Owner("Abhay")
    @Feature("PGP-31206")
    @Test(description = "verify bank name,PPI limit and verified merchant for PPI0 merchant using txn token in FetchPayOptions API")
    public void ValidateBankNameAndVerifiedMerchantForPPI0MerchantUsingFPO() throws Exception {
        Constants.MerchantType Ppi0 = Constants.MerchantType.NATIVE_HYBRID;
        MerchantProfile merchantProfile = new MerchantProfile(Ppi0.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("ppiLimit")).as("ppiLimit should be 0 for PPI0 merchant").isEqualTo("0");
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("merchantBankName should not be null").isNotNull();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Ppi0).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("Bank name doesn't match").isEqualTo(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantBankName"));
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.merchantDetails.verifiedMerchant")).as("verifiedMerchant should be true for PPI0 merchant").isEqualTo(true);

    }

    @Owner("Abhay")
    @Feature("PGP-31206")
    @Test(description = "verify bank name, PPI limit and verified merchant for PPI0 merchant using FetchQRPaymentDetails API")
    public void BankNameAndVerifiedMerchantForPPI0MerchantUsingFetchQRPaymentDetails() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Ppi0 = Constants.MerchantType.NATIVE_HYBRID;
        MerchantProfile merchantProfile = new MerchantProfile(Ppi0.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("ppiLimit")).as("pp1 limit should be 0 for PPI0 merchant").isEqualTo("0");
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("merchantBankName should not be null").isNotNull();
        GenerateQR generateQR = new GenerateQR(Ppi0.getId(), "");
        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Ppi0.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("Bank name doesn't match").isEqualTo(fetchQRResponse.getString("body.paymentOptions.merchantDetails.merchantBankName"));
        Assertions.assertThat(fetchQRResponse.getBoolean("body.paymentOptions.merchantDetails.verifiedMerchant")).as("verifiedMerchant should be true for PPI0 merchant").isEqualTo(true);

    }

    @Owner("Abhay")
    @Feature("PGP-31206")
    @Test(description = "verify bank name, PPI limit and verified merchant for PPI1 merchant using access token in FetchPaymentOptions API")
    public void BankNameAndVerifiedMerchantForPPI1MerchantFPOAccessToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Ppi0 = Constants.MerchantType.PPI1;
        MerchantProfile merchantProfile = new MerchantProfile(Ppi0.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("ppiLimit")).as("pp1 limit should be 1 for PPI1 merchant").isEqualTo("1");
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("merchantBankName should not be null").isNotNull();
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(Ppi0, user.ssoToken(), refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(Ppi0.getId()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Ppi0.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("Bank name doesn't match").isEqualTo(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantBankName"));
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.merchantDetails.verifiedMerchant")).as("verifiedMerchant should be false for PPI1 merchant").isEqualTo(false);
    }

    @Owner("Abhay")
    @Feature("PGP-31206")
    @Test(description = "verify bank name, PPI limit and verified merchant for PPI1 merchant using checksum token in FetchPaymentOptions API")
    public void BankNameAndVerifiedMerchantForPPI1MerchantFPOChecksum() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Ppi1 = Constants.MerchantType.PPI1;
        MerchantProfile merchantProfile = new MerchantProfile(Ppi1.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("ppiLimit")).as("pp1 limit should be 1 for PPI1 merchant").isEqualTo("1");
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("merchantBankName should not be null").isNotNull();
        String refId = UUID.randomUUID().toString().substring(0, 18);
        String body = "{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFID}\"," + "\"paytmSsoToken\":\"{SSOTOKEN}\"" + "}";
        body = body.replace("{MID}", Ppi1.getId())
                .replace("{REFID}", refId)
                .replace("{SSOTOKEN}", user.ssoToken());
        String Checksum = PGPUtil.getChecksum(Ppi1.getKey(), body);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM", Checksum).setReferenceId(refId)
                .setMid(Ppi1.getId()).setPaytmSsoToken(user.ssoToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Ppi1.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("Bank name doesn't match").isEqualTo(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantBankName"));
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.merchantDetails.verifiedMerchant")).as("verifiedMerchant should be false for PPI1 merchant").isEqualTo(false);
    }

    @Owner("Abhay")
    @Feature("PGP-31206")
    @Test(description = "verify bank name,PPI limit and verified merchant for PPI2 merchant using txn token in FetchPayOptions API")
    public void BankNameAndVerifiedMerchantForPPI2MerchantUsingFPO() throws Exception {
        Constants.MerchantType Ppi2 = Constants.MerchantType.PPI2;
        MerchantProfile merchantProfile = new MerchantProfile(Ppi2.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("ppiLimit")).as("ppiLimit should be 2 for PPI2 merchant").isEqualTo("2");
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("merchantBankName should not be null").isNotNull();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Ppi2).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("Bank name doesn't match").isEqualTo(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantBankName"));
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.merchantDetails.verifiedMerchant")).as("verifiedMerchant should be true for PPI2 merchant").isEqualTo(false);
    }

    @Owner("Abhay")
    @Feature("PGP-31206")
    @Test(description = "verify bank name, PPI limit and verified merchant for PPI2 merchant using checksum token in FetchPaymentOptions API")
    public void BankNameAndVerifiedMerchantForPPI2MerchantFPOChecksum() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Ppi2 = Constants.MerchantType.PPI2;
        MerchantProfile merchantProfile = new MerchantProfile(Ppi2.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("ppiLimit")).as("pp1 limit should be 2 for PPI2 merchant").isEqualTo("2");
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("merchantBankName should not be null").isNotNull();
        String refId = UUID.randomUUID().toString().substring(0, 18);
        String body = "{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFID}\"," + "\"paytmSsoToken\":\"{SSOTOKEN}\"" + "}";
        body = body.replace("{MID}", Ppi2.getId())
                .replace("{REFID}", refId)
                .replace("{SSOTOKEN}", user.ssoToken());
        String Checksum = PGPUtil.getChecksum(Ppi2.getKey(), body);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("CHECKSUM", Checksum).setReferenceId(refId)
                .setMid(Ppi2.getId()).setPaytmSsoToken(user.ssoToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Ppi2.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("merchantBankName")).as("Bank name doesn't match").isEqualTo(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantBankName"));
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.merchantDetails.verifiedMerchant")).as("verifiedMerchant should be false for PPI2 merchant").isEqualTo(false);
    }


}

