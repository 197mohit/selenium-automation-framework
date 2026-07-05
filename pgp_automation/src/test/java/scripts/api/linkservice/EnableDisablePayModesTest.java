package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Feature("PGP-36657")
public class EnableDisablePayModesTest extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }
    private WebDriver driver;

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Net Banking is visible on cashier page when we set enable payment mode as Net Banking for payment button.")
    public void PaymodeEanbleDisable_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("NET_BANKING");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"PAYMENT_BUTTON","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"PAYMENT_BUTTON","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),true);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
    }

   @Parameters({"theme"})
   @Owner("Himanshu Arora")
    @Test(description = "Verify Only Debit Card Is Visible On Cashier Page When We Set Enable Paymode is Debit Card For Generic Link")
    public void PaymodeEanbleDisable_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
       List<String> channels= new ArrayList<>();
       List<String> enablePaymode= new ArrayList<>();
       List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("DEBIT_CARD");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
       createNewLinkEnableDisable.buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
       String availablePaymentOptions = cashierPage.paymentOptions().getText();
       Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),true);
       Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
       Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
       Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
       Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only UPI Is Visible On Cashier Page When We Set Enable Paymode is UPI For Fixed Link")
    public void PaymodeEanbleDisable_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("UPI");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),true);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only Wallet Is Visible On Cashier Page When We Set Enable Paymode is Wallet For Fixed Link")
    public void PaymodeEanbleDisable_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("PPI");
        user = userManager.getForWrite(Label.AUTOLINK);
        mid=Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString();
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertEquals("Paytm Balance",cashierPage.PaymodeEnableDisableWalletMessage().getText(),"Validate Only Wallet Is Visible On Cashier Page");
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only Credit Card Is Visible On Cashier Page When We Set Enable Paymode is credit card For Invoice Link")
    public void PaymodeEanbleDisable_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("CREDIT_CARD");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),true);
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only EMI Is Visible On Cashier Page When We Set Enable Paymode as EMI For Fixed Link")
    public void PaymodeEanbleDisable_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("EMI");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),true);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only Paytm Postpaid Is Visible On Cashier Page When We Set Enable Paymode is Paytm Postpaid For Fixed Link")
    public void PaymodeEanbleDisable_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("PAYTM_DIGITAL_CREDIT");
        user = userManager.getForWrite(Label.AUTOLINK);
        mid=Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString();
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertEquals("Paytm Postpaid",cashierPage.PaymodeEnableDisablePostpaidMessage().getText(),"Validate Only Paytm Postpaid Is Visible On Cashier Page");
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only UPI Is Visible On Cashier Page When We Set Enable Paymode is Credit Card and UPI,& disable payment mode is Credit Card For Generic Link")
    public void PaymodeEanbleDisable_08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("CREDIT_CARD");
        enablePaymode.add("UPI");
        disablePaymode.add("CREDIT_CARD");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"GENERIC","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"GENERIC","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),true);
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only Wallet Is Visible On Cashier Page When We Set Enable Paymode is Debit Card and Wallet,& disable pay mode is Debit card For Generic Link")
    public void PaymodeEanbleDisable_09(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("DEBIT_CARD");
        enablePaymode.add("EMI");
        disablePaymode.add("DEBIT_CARD");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),true);
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only Net Banking Is Visible On Cashier Page When We Set Enable Paymode is Debit Card & Net Banking,& disable pay mode is Debit card For Invoice Link")
    public void PaymodeEanbleDisable_10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("DEBIT_CARD");
        enablePaymode.add("NET_BANKING");
        disablePaymode.add("DEBIT_CARD");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMODE.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"INVOICE","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"INVOICE","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),true);
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "All the pay modes are displayed on cashier page that are configured on merchant in case of offline link.")
    public void PaymodeEanbleDisable_11(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_OFFLINE_LINK.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"FIXED","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"FIXED","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptionsOfflineFlow().getText();
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),true);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),true);
    }

    @Parameters({"theme"})
    @Owner("Himanshu Arora")
    @Test(description = "Verify Only Net Banking Is Visible On Cashier Page When We Set Enable Paymode is Debit Card & Net Banking,& disable pay mode is Debit card For Invoice Link")
    public void PaymodeEanbleDisable_12(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        List<String> channels= new ArrayList<>();
        List<String> enablePaymode= new ArrayList<>();
        List<String> disablePaymode= new ArrayList<>();
        enablePaymode.add("UPI");
        enablePaymode.add("NET_BANKING");
        disablePaymode.add("UPI");
        setUserAndMId(Constants.MerchantType.ENABLE_DISABLE_PAYMENT_BUTTON.getId().toString());
        CreateNewLink createNewLinkEnableDisable = new CreateNewLink(enablePaymode,disablePaymode);
        createNewLinkEnableDisable.buildRequest(mid,"PAYMENT_BUTTON","2000");
        JsonPath withDrawJson1 = createNewLinkEnableDisable.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTPForLink(user,paymentLink,"PAYMENT_BUTTON","web");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String availablePaymentOptions = cashierPage.paymentOptions().getText();
        Assert.assertEquals(availablePaymentOptions.contains("Net Banking"),true);
        Assert.assertEquals(availablePaymentOptions.contains("EMI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("UPI"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Credit Card"),false);
        Assert.assertEquals(availablePaymentOptions.contains("Debit Card"),false);
    }
}
