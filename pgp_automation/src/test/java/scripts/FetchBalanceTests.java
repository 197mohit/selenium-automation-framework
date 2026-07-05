package scripts;

import ch.qos.logback.core.db.dialect.SybaseSqlAnywhereDialect;
import com.paytm.api.FastForward;
import com.paytm.api.FetchBalance;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.DecimalFormat;

import static com.paytm.appconstants.Constants.Owner.VIKASH_VERMA;
import static com.paytm.apphelpers.QRHelper.generateQRViaWallet;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;


public class FetchBalanceTests extends PGPBaseTest {

    @Owner("Abhay")
    @Feature("PGP-30746")
    @Test(description = "Validate fetch Balance by sending mid only in params and not in body for offus merchant")
    public void fetchBalanceWithoutMidInBodyForOffusMerchant() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType PpblMerchant = Constants.MerchantType.PPBLYONLY;

        //to check whether merchant is offus or not
        Boolean isOffus = new MigrationDetails(PpblMerchant.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOffus).as("Merchant is not offus").isEqualTo(false);

        FetchBalance fetchBalance  = new FetchBalance(PpblMerchant.getId(), CommonHelpers.generateOrderId(),user.ssoToken(), "PPBL");
        fetchBalance.deleteContext("body.mid");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("ACTIVE");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo")).as("balanceInfo in fetchBalance API response is null").isNotNull();

    }

    @Owner("Abhay")
    @Feature("PGP-30746")
    @Test(description = "Validate fetch Balance by sending mid both in params as well as in body for offus merchant")
    public void fetchBalanceWithMidInParamsAndBodyForOffusMerchant() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType PpblMerchant = Constants.MerchantType.PPBLYONLY;

        //to check whether merchant is offus or not
        Boolean isOffus = new MigrationDetails(PpblMerchant.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOffus).as("Merchant is not offus").isEqualTo(false);

        FetchBalance fetchBalance  = new FetchBalance(PpblMerchant.getId(), CommonHelpers.generateOrderId(),user.ssoToken(), "PPBL");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("ACTIVE");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo")).as("balanceInfo in fetchBalance API response is null").isNotNull();

    }

    @Owner("Abhay")
    @Feature("PGP-30746")
    @Test(description = "Validate fetch Balance by sending mid only in params and not in body for onus merchant")
    public void fetchBalanceWithoutMidInBodyForOnusMerchant() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType PcfOnus = Constants.MerchantType.PCF_ONUS;

        //to check whether merchant is onus or not
        Boolean isOnus = new MigrationDetails(PcfOnus.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOnus).as("Merchant is not onus").isEqualTo(true);

        FetchBalance fetchBalance  = new FetchBalance(PcfOnus.getId(), CommonHelpers.generateOrderId(),user.ssoToken(), "PPBL");
        fetchBalance.deleteContext("body.mid");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("ACTIVE");
        Assertions.assertThat(fetchBalanceResponse.getString("body.isRedemptionAllowed")).as("isRedemptionAllowed in fetchBalance API response is expected to be true").isEqualTo("true");
        Assertions.assertThat(fetchBalanceResponse.getString("body.partnerBankBalances")).as("partnerBankBalances in fetchBalance API response is not expected to be null").isNotNull();
        Assertions.assertThat(fetchBalanceResponse.getString("body.investmentTnCUrl")).as("investmentTnCUrl in fetchBalance API response does not march").isEqualTo("https://kyc.paytmbank.com/kyc/tnc/get/1051");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo")).as("balanceInfo in fetchBalance API response is not expected to be null").isNotNull();

    }

    @Owner("Abhay")
    @Feature("PGP-30746")
    @Test(description = "Validate fetch Balance by sending mid both in params as well as in body for onus merchant")
    public void fetchBalanceWithMidInParamsAndBodyForOnusMerchant() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType PcfOnus = Constants.MerchantType.PCF_ONUS;

        //to check whether merchant is onus or not
        Boolean isOnus = new MigrationDetails(PcfOnus.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOnus).as("Merchant is not onus").isEqualTo(true);

        FetchBalance fetchBalance  = new FetchBalance(PcfOnus.getId(), CommonHelpers.generateOrderId(),user.ssoToken(), "PPBL");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("ACTIVE");
        Assertions.assertThat(fetchBalanceResponse.getString("body.isRedemptionAllowed")).as("isRedemptionAllowed in fetchBalance API response is expected to be true").isEqualTo("true");
        Assertions.assertThat(fetchBalanceResponse.getString("body.partnerBankBalances")).as("partnerBankBalances in fetchBalance API response is not expected to be null").isNotNull();
        Assertions.assertThat(fetchBalanceResponse.getString("body.investmentTnCUrl")).as("investmentTnCUrl in fetchBalance API response does not march").isEqualTo("https://kyc.paytmbank.com/kyc/tnc/get/1051");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo")).as("balanceInfo in fetchBalance API response is not expected to be null").isNotNull();

    }
    @Owner("Aayush")
    @Feature("PGP-32146")
    @Test(description = "Validate fetch Balance by sending mid both in params as well as in body for offus merchant ff4j flag based")
    public void fetchBalanceForOffusMerchant() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType Offus = Constants.MerchantType.PG_OFFUS;

        // FF4J flags should be controlled based on Mid from FF4J dashboard and not within TCs
        //   FF4JFlags.enableMidBased("theia.OffUs.useInvestmentAsFundingSource",Offus.getId() );

        //to check whether merchant is offus or not
        Boolean isOffus = new MigrationDetails(Offus.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOffus).as("Merchant is not offus").isEqualTo(false);

        FetchBalance fetchBalance  = new FetchBalance(Offus.getId(), CommonHelpers.generateOrderId(),user.ssoToken(), "PPBL");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).as("resultMsg in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).as("resultCode in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).as("resultStatus in fetchBalanceInfo API response is incorrect for invalid pay Mode").isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("ACTIVE");
        Assertions.assertThat(fetchBalanceResponse.getString("body.isRedemptionAllowed")).as("isRedemptionAllowed in fetchBalance API response is expected to be true").isEqualTo("true");
        Assertions.assertThat(fetchBalanceResponse.getString("body.partnerBankBalances")).as("partnerBankBalances in fetchBalance API response is not expected to be null").isNotNull();
        Assertions.assertThat(fetchBalanceResponse.getString("body.investmentTnCUrl")).as("investmentTnCUrl in fetchBalance API response does not march").isEqualTo("https://kyc.paytmbank.com/kyc/tnc/get/1051");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo")).as("balanceInfo in fetchBalance API response is not expected to be null").isNotNull();

    }
    @Owner("Vidhi")
    @Feature("PGP-39034")
    @Test(description = "Validate fetch balance Info Api|SSO_TOKEN|LOYALTY_POINTS when ff4j flag theia.loyaltyPointsMigration - TRUE")
    public void fetchBalanceForLoyaltyPoints_SSOTOKEN() throws Exception
    {
        User user = userManager.getForRead(Label.LOYALTYPOINT);
        Constants.MerchantType mid = Constants.MerchantType.LOYALTY_POINT;
        FetchBalance fetchBalance=new FetchBalance(mid.getId(),CommonHelpers.generateOrderId(),user.ssoToken(),"LOYALTY_POINT");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        System.out.println(fetchBalanceResponse);
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.availablePoints")).isNotNull();
        Assertions.assertThat(fetchBalanceResponse.getString("body.exchangeRate")).isEqualTo("100");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo.currency")).isEqualTo("INR");
        double availablePoints=Double.parseDouble(fetchBalanceResponse.getString("body.availablePoints"));
        double exchangeRate=Double.parseDouble(fetchBalanceResponse.getString("body.exchangeRate"));
        double balanceInfo=availablePoints/exchangeRate;
        double balanceInfo_value=Double.parseDouble(fetchBalanceResponse.getString("body.balanceInfo.value"));
        Assert.assertEquals(balanceInfo,balanceInfo_value);
    }
    @Owner("Vidhi")
    @Feature("PGP-39034")
    @Test(description = "Validate fetch balance Info Api|TXN_TOKEN|LOYALTY_POINTS when ff4j flag theia.loyaltyPointsMigration - TRUE")
    public void fetchBalanceForLoyaltyPoints_TXNTOKEN() throws Exception
    {
        String price ="1";
        User user = userManager.getForRead(Label.LOYALTYPOINT);
        Constants.MerchantType mid = Constants.MerchantType.LOYALTY_POINT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),mid)
                .setTxnValue(price)
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");
        FetchBalance fetchBalance=new FetchBalance(mid.getId(),initTxnDTO.orderFromBody(),txnToken,"LOYALTY_POINT","abc");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        System.out.println(fetchBalanceResponse);
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.availablePoints")).isNotNull();
        Assertions.assertThat(fetchBalanceResponse.getString("body.exchangeRate")).isEqualTo("100");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo.currency")).isEqualTo("INR");
        double availablePoints=Double.parseDouble(fetchBalanceResponse.getString("body.availablePoints"));
        double exchangeRate=Double.parseDouble(fetchBalanceResponse.getString("body.exchangeRate"));
        double balanceInfo=availablePoints/exchangeRate;
        double balanceInfo_value=Double.parseDouble(fetchBalanceResponse.getString("body.balanceInfo.value"));
        Assert.assertEquals(balanceInfo,balanceInfo_value);
    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47530")
    @Test(description = "validate Fetch balance for PPBL paymode with orderid in query param")
    public void fetchBalanceForPPBL() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String qrCodeId = generateQRViaWallet(merchant);

        FetchBalance fetchBalance=new FetchBalance();
        fetchBalance.qrcodeidwithqueryparam(qrCodeId,CommonHelpers.generateOrderId(),user.ssoToken(),"PPBL");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo.currency")).isEqualTo("INR");


    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47530")
    @Test(description = "validate Fetch balance for wallet paymode with orderid in query param ")
    public void fetchBalanceForwallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String qrCodeId = generateQRViaWallet(merchant);

        FetchBalance fetchBalance=new FetchBalance();
        fetchBalance.qrcodeidwithqueryparam(qrCodeId,CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo.currency")).isEqualTo("INR");

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47530")
    @Test(description = "validate Fetch balance for wallet paymode when mid don't have wallet configured with orderid in query param")
    public void fetchBalanceForwalletwhenmidnothavewallet() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode;
        String qrCodeId = generateQRViaWallet(merchant);

        FetchBalance fetchBalance=new FetchBalance();
        fetchBalance.qrcodeidwithqueryparam(qrCodeId,CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("2025");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Merchant does not support this payment mode");

    }

    @Feature("PGP-47530")
    @Test(description = "validate Fetch balance for wallet paymode when mid qrcodeid is wrong with orderid in query param")
    public void fetchBalanceForwalletwhenqrcodeidiswrong() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode;
        String qrCodeId = generateQRViaWallet(merchant);
        qrCodeId=qrCodeId.substring(0,6);

        FetchBalance fetchBalance=new FetchBalance();
        fetchBalance.qrcodeidwithqueryparam(qrCodeId,CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("2017");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("QrCodeId is invalid");

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47530")
    @Test(description = "validate Fetch balance for wallet paymode without query param ")
    public void fetchBalanceForwalletwithoutqueryparam() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 100.0);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        String qrCodeId = generateQRViaWallet(merchant);

        FetchBalance fetchBalance=new FetchBalance();
        fetchBalance.qrcodeidwithoutqueryparam(qrCodeId,CommonHelpers.generateOrderId(),user.ssoToken(),"BALANCE");
        JsonPath fetchBalanceResponse=fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBalanceResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBalanceResponse.getString("body.balanceInfo.currency")).isEqualTo("INR");

    }

}