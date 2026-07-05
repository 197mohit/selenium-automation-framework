package scripts.coft.theia;

import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.time.Instant;

public class GlobalVaultSplit extends PGPBaseTest{
    public static User user;
    public static String custId;
    public static String txnAmount;
    public static Constants.MerchantType oclMid_N = Constants.MerchantType.COFT_THEIA_ONUS;
    public static String oclToken;
    public static  Constants.MerchantType addmoneyMid_N = Constants.MerchantType.GV_SPLIT_ADDMONEY;
    public static String addMoneyToken;
    public static Constants.MerchantType lvMid_N = Constants.MerchantType.GV_SPLIT_LV;
    public static Constants.MerchantType oclMid_Y = Constants.MerchantType.GV_SPLIT_OCL_GVC_ENABLED;
    public static Constants.MerchantType addmoneyMid_Y = Constants.MerchantType.GV_SPLIT_ADDMONEY_GVC_ENABLED;
    public static Constants.MerchantType lvMid_Y = Constants.MerchantType.GV_SPLIT_LV;
    public static Constants.MerchantType pMallMid = Constants.MerchantType.GV_SPLIT_PMALL;
    public static String pMallToken;

    @BeforeClass
    public void setUp() throws Exception {
        user = userManager.getForWrite(PGPBaseTest.Label.LOGIN);
        custId = RandomStringUtils.randomAlphabetic(4)+ Instant.now().toEpochMilli();
        txnAmount = Double.toString(WalletHelpers.getWalletBalance(user)+2);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO oclPaymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,oclPaymentDTO.getExpMonth(),oclPaymentDTO.getExpYear(), oclPaymentDTO.getCreditCardNumber());
        oclToken = SavedCardHelpers.getTin();
        PaymentDTO addMoneyPaymentDTO = new PaymentDTO();
        SavedCardHelpers.addCardInOtherVaults(user,addMoneyPaymentDTO.getExpMonth(),addMoneyPaymentDTO.getExpYear(),addMoneyPaymentDTO.getCreditCardNumber(),"PPBL");
        addMoneyToken = SavedCardHelpers.getTin();
        PaymentDTO pmallPaymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,pmallPaymentDTO.getExpMonth(),pmallPaymentDTO.getExpYear(), pmallPaymentDTO.getCreditCardNumber());
        pMallToken = SavedCardHelpers.getTin();
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addNPay transaction with ocl mid")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithOclMid(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), oclMid_N).setCustId(user.custId()).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(oclMid_N.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(oclToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addNPay transaction with ppbl mid")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithPPBLMid(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addmoneyMid_N).setCustId(user.custId()).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(addmoneyMid_N.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(oclToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addMoney transaction with ppbl mid")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addMoneyWithPPBLMid(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addmoneyMid_N).setCustId(user.custId()).setIsNativeAddMoney("true").setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(addmoneyMid_N.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(oclToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addNPay transaction with offus mid")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithOffusMid(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), lvMid_N).setCustId(custId).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(lvMid_N.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addMoney transaction with offus mid")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addMoneyWithOffusMid(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), lvMid_N).setCustId(user.custId()).setIsNativeAddMoney("true").setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(lvMid_N.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addNPay transaction with ocl mid with GLOBAL_VAULT_COFT preference as 'Y'")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithOclMidwithGlobalVaultCoftEnabled(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), oclMid_Y).setCustId(user.custId()).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(oclMid_Y.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(oclToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addNPay transaction with ppbl mid GLOBAL_VAULT_COFT preference as 'Y'")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithPPBLMidwithGlobalVaultCoftEnabled(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addmoneyMid_Y).setCustId(user.custId()).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(addmoneyMid_Y.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(oclToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addMoney transaction with ppbl mid with GLOBAL_VAULT_COFT preference as 'Y'")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addMoneyWithPPBLMidwithGlobalVaultCoftEnabled(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addmoneyMid_Y).setCustId(user.custId()).setIsNativeAddMoney("true").setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(addmoneyMid_Y.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(oclToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addNPay transaction with offus mid with GLOBAL_VAULT_COFT preference as 'Y'")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithOffusMidwithGlobalVaultCoftEnabled(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), lvMid_Y).setCustId(custId).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(lvMid_Y.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify addMoney Token is Present in FPO response for addMoney transaction with offus mid with GLOBAL_VAULT_COFT preference as 'Y'")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addMoneyWithOffusMidwithGlobalVaultCoftEnabled(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), lvMid_Y).setCustId(user.custId()).setIsNativeAddMoney("true").setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(lvMid_Y.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(addMoneyToken);
    }

    @Test(description = "Verify pMall Token is Present in FPO response for addNPay transaction with pMall mid")
    @Owner(Constants.Owner.RUPASANANDA)
    @Feature("PGP-49602")
    public static void addNPayWithPMallMid(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pMallMid).setCustId(user.custId()).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponseDTO.getBody().getTxnToken()).build();
        FetchPaymentOptionV2 fetchPaymentOptionV2 = new FetchPaymentOptionV2(pMallMid.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionV2Response = fetchPaymentOptionV2.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.merchantPayOption")).contains(pMallToken);
        Assertions.assertThat(fetchPaymentOptionV2Response.getString("body.addMoneyPayOption")).contains(addMoneyToken);
    }
}