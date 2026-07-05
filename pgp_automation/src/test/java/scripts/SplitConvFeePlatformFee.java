package scripts;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;
import scripts.Native.checkoutjs.CheckoutJsBase;

import static com.paytm.appconstants.Constants.Owner.ROHIT_SHARMA;
import static com.paytm.appconstants.Constants.Owner.VIDHI;

public class SplitConvFeePlatformFee extends CheckoutJsBase {
    private final CheckoutJsCheckoutPage checkoutJSPage = new CheckoutJsCheckoutPage();
    private final CheckoutPage checkoutPage = new CheckoutPage();


    //    **************************************** THEIA PPSL-719 **************************************************
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245")
    @Test(description = "Verify the Convenience Charges in fetchPcfDetails API Response")
    public void verifyConvFeeChargesAPIResponse(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.PCF_PLATFORM_MID)
                .setTxnValue(String.valueOf("1000"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        System.out.println("txnToken = "+txnToken);
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING","ICICI",txnToken,initTxnDTO);
        System.out.println("jsonPath - "+"\n");
        System.out.println(jsonPath);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.NET_BANKING.convenienceCharges.value")).isNotNull();
    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245")
    @Test(description = "Verify the Convenience Charges Text in fetchPcfDetails API Response")
    public void verifyConvFeeChargesText(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.PCF_PLATFORM_MID)
                .setTxnValue(String.valueOf("1000"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        System.out.println("txnToken = "+txnToken);
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING","ICICI",txnToken,initTxnDTO);
        System.out.println("jsonPath - "+"\n");
        System.out.println(jsonPath);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.NET_BANKING.convenienceFeeText")).isEqualTo("Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently.");
    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245")
    @Test(description = "Verify the Platform Charges  in fetchPcfDetails API Response")
    public void verifyPlatformFeeChargesAPIResponse(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.PCF_PLATFORM_MID)
                .setTxnValue(String.valueOf("1000"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        System.out.println("txnToken = "+txnToken);
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING","ICICI",txnToken,initTxnDTO);
        System.out.println("jsonPath - "+"\n");
        System.out.println(jsonPath);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.NET_BANKING.platformCharges.value")).isNotNull();
    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245")
    @Test(description = "Verify the Platform Charges Text in fetchPcfDetails API Response")
    public void verifyPlatformFeeChargesText(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.PCF_PLATFORM_MID)
                .setTxnValue(String.valueOf("1000"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        System.out.println("txnToken = "+txnToken);
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING","ICICI",txnToken,initTxnDTO);
        System.out.println("jsonPath - "+"\n");
        System.out.println(jsonPath);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.NET_BANKING.platformFeeText")).isEqualTo("Platform fees refer to charges levied by PG on end customers. These fees are instrument agnostic and cover the infrastructure costs of maintaining platform.");
    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245")
    @Test(description = "Verify the Platform Charges in fetchPcfDetails API Response when Conv Fee is enabled but platForm fee is 0 or not enabled on MID")
    public void verifyPlatformFeeChargesAPIResponse_0(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.PCF_PLATFORM_MID)
                .setTxnValue(String.valueOf("1000"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        System.out.println("txnToken = "+txnToken);
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("DEBIT_CARD",null,txnToken,initTxnDTO);
        System.out.println("jsonPath - "+"\n");
        System.out.println(jsonPath);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.DEBIT_CARD.platformCharges.value")).isEqualTo("0.00");
    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245")
    @Test(description = "Verify the Conv Fee and Platform Fee Charges in fetchPcfDetails API Response when both Fees are 0 on paymode")
    public void verifyConvFeePlatformFeeCharges_0(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,Constants.MerchantType.PG2_ENABLED_PCF_Platform_MID)
                .setTxnValue(String.valueOf("1000"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        System.out.println("txnToken = "+txnToken);
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("CREDIT_CARD",null,txnToken,initTxnDTO);
        System.out.println("jsonPath - "+"\n");
        System.out.println(jsonPath);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.CREDIT_CARD.convenienceCharges.value")).isEqualTo("0.00");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.CREDIT_CARD.platformCharges.value")).isEqualTo("0.00");
    }

    // ------------------------------------------ UI cases PPSL-720 ------------------------------------
    //-------------------------------CHECKOUT JS --------------------------------------

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Fees Applied text on Net Banking tab in Checkout JS")
    public void verifyFees_applied_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_PLATFORM_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.feesAppliedText().getText()).isEqualTo("Fees Applied");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the iButton on Net Banking tab in Checkout JS")
    public void verify_i_button_heading(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_PLATFORM_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_Heading().getText()).isEqualTo("Fees Applied");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Conv Fee option on clicking the i-Button on Checkout JS UI")
    public void verify_ibuttonConvFeeHeading(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_PLATFORM_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_ConvFeeHeading().getText()).isEqualTo("Convenience Fee");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Conv Fee Text on clicking the i-Button on Checkout JS UI")
    public void verify_ibuttonConvFeeText(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_PLATFORM_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_ConvFeeText().getText()).isEqualTo("Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently.");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Platform Fee option on clicking the i-Button on Checkout JS UI")
    public void verify_ibuttonPlatformFeeHeading(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_PLATFORM_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_PlatformFeeHeading().getText()).isEqualTo("Platform Fee");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6245/PPSL-720")
    @Parameters({"theme"})
    @Test(description = "Verify the Platform Fee Text on clicking the i-Button on Checkout JS UI")
    public void verify_ibuttonPlatformFeeText(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_PLATFORM_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJSPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJSPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        Assertions.assertThat(cashierPage.iButton_PlatformFeeText().getText()).isEqualTo("Platform fees refer to charges levied by PG on end customers. These fees are instrument agnostic and cover the infrastructure costs of maintaining platform.");
    }

}
