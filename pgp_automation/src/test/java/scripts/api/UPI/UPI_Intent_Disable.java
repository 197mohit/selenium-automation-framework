package scripts.api.UPI;

import com.paytm.api.RedisAPI;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.apphelpers.supercashhelpers.superCashHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.luaj.vm2.ast.Str;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class UPI_Intent_Disable extends PGPBaseTest {
    // Imp Note: If addNPayMerchantsAffected =null in bolck/config API we will block for All (ONUS and 3P online)
    final String  IOSUserAgent="Paytm Release/10.15.0/101500 (com.one97.paytm.all.payments; source=appstore; integrity=false; auth=false; en-IN; jarvis-network-ios 10.11.0) iOS/15.2 iPhone/iPhone 7 Plus (arm64; resolution=3.0 cores=2)";
    final String  androidUserAgent="Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";
    final String androidClient="andriodapp";
    final String IOSClient="iosApp";
    final String checkout="checkout",enhancedCashierFlow="enhancedCashierFlow";

    @BeforeClass
    public void enableFF4J()
    {
        System.out.println("before class");
        FF4JFlags.enable("theia.enablePayModeFilterOnBossViaMC");
        RedisAPI.deleteKey("F4J_FEATURE_theia.enablePayModeFilterOnBossViaM");
    }
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption for Android")
    public void UPI_INTENTDisabled_FPOV5_Android_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("APP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), androidClient,androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V2 for Android")
    public void UPI_INTENTDisabled_FPOV2_Android_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), androidClient,androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V1 for Android")
    public void UPI_INTENTDisabled_FPOV1_Android_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), androidClient,androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption  for Android")
    public void UPI_INTENTDisabled_FPOV5_Android_WebView() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setWorkFlow(checkout).setChannelId("WEB").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V2 for Android")
    public void UPI_INTENTDisabled_FPOV2_Android_WebView() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V1 for Android")
    public void UPI_INTENTDisabled_FPOV1_Android_WebView() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption  for Android")
    public void UPI_INTENTDisabled_FPOV5_Android_MWeb() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V2 for Android")
    public void UPI_INTENTDisabled_FPOV2_Android_MWeb() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V1 for Android")
    public void UPI_INTENTDisabled_FPOV1_Android_MWeb() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    // OS IOS

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption for IOS APP")
    public void UPI_INTENTDisabled_FPOV5_IOS_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        //FF4JFlags.enableMidBased("theia.enablePayModeFilterOnBossViaMC", merchant.getId());
        //FF4JFlags.enable("theia.enablePayModeFilterOnBossViaMC");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V2 for IOS APP")
    public void UPI_INTENTDisabled_FPOV2_IOS_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("APP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V1 for IOS APP")
    public void UPI_INTENTDisabled_FPOV1_IOS_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(checkout).setChannelId("APP").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption  for IOS WebView")
    public void UPI_INTENTDisabled_FPOV5_IOS_WebView() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WEB").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V2 for IOS")
    public void UPI_INTENTDisabled_FPOV2_IOS_WebView() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WEB").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V1 for IOS")
    public void UPI_INTENTDisabled_FPOV1_IOS_WebView() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setChannelId("WEB").setWorkFlow(checkout).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption  for IOS")
    public void UPI_INTENTDisabled_FPOV5_IOS_MWeb() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V2 for IOS")
    public void UPI_INTENTDisabled_FPOV2_IOS_MWeb() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in merchantPayOption in FPO V1 for IOS")
    public void UPI_INTENTDisabled_FPOV1_IOS_MWeb() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is enabled in merchantPayOption for IOS when workFlow is null in FPO request")
    public void UPI_INTENTEnabled_FPOV5_IOS_MWeb_nullWorkFlow() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        System.out.println("checking size: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()")).isEqualTo("3");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode")).contains("UPIPUSH");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.find {it.channelCode == 'UPIPUSH'}.channelName")).isEqualTo("Unified Payment Interface - PUSH");
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is enabled in merchantPayOption for Andriod when workFlow is null in FPO request")
    public void UPI_INTENTEnabled_FPOV5_Andriod_MWeb_nullWorkFlow() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        System.out.println("checking size: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()")).isEqualTo("3");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode")).contains("UPIPUSH");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.find {it.channelCode == 'UPIPUSH'}.channelName")).isEqualTo("Unified Payment Interface - PUSH");
        //Validate_UPIINTENT_Enable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    //Check UPI INTENT is disabled in addMoneyPayOption


    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption for Android App")
    public void UPI_INTENTDisabled_FPOV5_addMoneyPayOption_Android_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("APP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), androidClient,androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V2 for Android APP")
    public void UPI_INTENTDisabled_FPOV2_addMoneyPayOption_Android_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), androidClient,androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V1 for Android App")
    public void UPI_INTENTDisabled_FPOV1_addMoneyPayOption_Android_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), androidClient,androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);

    }

    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption for Android WAP")
    public void UPI_INTENTDisabled_FPOV5_addMoneyPayOption_Android_WAP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V2 for Android WAP")
    public void UPI_INTENTDisabled_FPOV2_addMoneyPayOption_Android_WAP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V1 for Android WAP")
    public void UPI_INTENTDisabled_FPOV1_addMoneyPayOption_Android_WAP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);

    }

    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption for Android WEB")
    public void UPI_INTENTDisabled_FPOV5_addMoneyPayOption_Android_WEB() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WEB").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V2 for Android WEB")
    public void UPI_INTENTDisabled_FPOV2_addMoneyPayOption_Android_WEB() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WEB").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V1 for Android WEB")
    public void UPI_INTENTDisabled_FPOV1_addMoneyPayOption_Android_WEB() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").setWorkFlow(checkout).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),androidUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);

    }

    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption for IOS App")
    public void UPI_INTENTDisabled_FPOV5_addMoneyPayOption_IOS_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("APP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V2 for IOS APP")
    public void UPI_INTENTDisabled_FPOV2_addMoneyPayOption_IOS_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V1 for IOS App")
    public void UPI_INTENTDisabled_FPOV1_addMoneyPayOption_IOS_APP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);

    }

    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption for IOS WAP")
    public void UPI_INTENTDisabled_FPOV5_addMoneyPayOption_IOS_WAP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("200.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V2 for IOS WAP")
    public void UPI_INTENTDisabled_FPOV2_addMoneyPayOption_IOS_WAP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WAP").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V1 for IOS WAP")
    public void UPI_INTENTDisabled_FPOV1_addMoneyPayOption_IOS_WAP() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WAP").setWorkFlow(checkout).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);

    }

    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption for IOS WEB")
    public void UPI_INTENTDisabled_FPOV5_addMoneyPayOption_IOS_WEB() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).setChannelId("WEB").setWorkFlow(checkout).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V2 for IOS WEB")
    public void UPI_INTENTDisabled_FPOV2_addMoneyPayOption_IOS_WEB() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").setIsLiteEligible(true).setChannelId("WEB").setWorkFlow(enhancedCashierFlow).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-49018")
    @Test(description = "Verify UPI INTENT is disabled in addMoneyPayOption in FPO V1 for IOS WEB")
    public void UPI_INTENTDisabled_FPOV1_addMoneyPayOption_IOS_WEB() throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.UPIINTENT_DISABLE_ADDNPAY;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setWorkFlow(enhancedCashierFlow).setChannelId("WEB").setWorkFlow(checkout).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(),IOSClient,IOSUserAgent, fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Validate_UPIINTENT_IN_MerchantPayOption(fetchPaymentOptionsJson);
        //Validate_UPIINTENT_Disable_AddMoneyPayOption(fetchPaymentOptionsJson);

    }
    public static void Validate_UPIINTENT_IN_MerchantPayOption(JsonPath fetchPaymentOptionsJson) {
        System.out.println("checking size: "+fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()"));
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()")).isEqualTo("2");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode")).isNotEqualTo("UPIPUSH");

    }

    public static void Validate_UPIINTENT_Enable_AddMoneyPayOption(JsonPath fetchPaymentOptionsJson) {
        boolean containsUPIPUSH = false;
        String[] paychannel = fetchPaymentOptionsJson.getString("body.addMoneyPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode").replaceAll("\\[|\\]", "").split(", ");
        for (String item : paychannel) {
            if (item.equals("UPIPUSH"))
            {
                containsUPIPUSH = true;
                break;
            }
        }
        Assert.assertTrue(containsUPIPUSH, "UPIPUSH is not present in addMoneyPayOption");
    }

   public static void Validate_UPIINTENT_Disable_AddMoneyPayOption(JsonPath fetchPaymentOptionsJson) {
       boolean iscontainsUPIPUSH = true;
       String[] paychannel = fetchPaymentOptionsJson.getString("body.addMoneyPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode").replaceAll("\\[|\\]", "").split(", ");
       System.out.println("Items: "+paychannel);
       for (String item : paychannel) {
           if (item.equals("UPIPUSH"))
           {
               iscontainsUPIPUSH = false;
               break;
           }
       }
       Assert.assertTrue(iscontainsUPIPUSH, "UPIPUSH is present in addMoneyPayOption");
   }

}
