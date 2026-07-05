package scripts.api.linkservice;

import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.Merchant;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;



public class EdcEmiLink extends PGPBaseTest {
    User user;
    String mid;
    void setUserAndMId(String merchant) throws Exception {
        user = userManager.getForWrite(Label.LINK);
        mid = merchant;
    }


    @Owner("Abhishek Gupta")
    @Feature("PGP-35107")
    @Parameters({"theme"})
    @Test(description = "verify that create EDC Brand EMI link and validate from theia_facade logs ")
    public void Initiate_Txn_001(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.Edc_Emi.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true).buildRequest(mid,"FIXED","4600");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink,user);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardNoBox().sendKeys("4854980821814649");
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys("05");
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys("30");
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("123");
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"edcLinkTxn\":\"true\"");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-35107")
    @Parameters({"theme"})
    @Test(description = "verify that create EDC Bank EMI link and validate from theia_facade logs")
    public void Initiate_Txn_002(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.Edc_Emi.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","4600");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink,user);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardNoBox().sendKeys("4854980821814649");
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys("05");
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys("30");
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("123");
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"edcLinkTxn\":\"true\"");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-35107")
    @Parameters({"theme"})
    @Test(description = "verify that create EDC Brand EMI link and validate from theia_facade logs ")
    public void Initiate_Txn_003(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.Edc_Emi.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true).buildRequest(mid,"FIXED","4600");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink,user);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardNoBox().sendKeys("4854980821814649");
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys("05");
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys("30");
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("123");
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"emiType\":\"BRAND_EMI\"");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-35107")
    @Parameters({"theme"})
    @Test(description = "verify that create EDC Bank EMI link and validate from theia_facade logs")
    public void Initiate_Txn_004(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.Edc_Emi.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","4600");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink,user);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardNoBox().sendKeys("4854980821814649");
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys("05");
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys("30");
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("123");
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid,"ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"emiType\":\"BANK_EMI\"");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-35107")
    @Parameters({"theme"})
    @Test(description = "verify that create EDC Brand EMI link and validate from theia_facade logs ")
    public void Initiate_Txn_005(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.Edc_Emi.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true).buildRequest(mid,"FIXED","4600");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink,user);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardNoBox().sendKeys("4854980821814649");
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys("05");
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys("30");
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("123");
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        String log_brandEmi= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid,"BRAND_EMI");
        Assert.assertNotNull(log_brandEmi);
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-35107")
    @Parameters({"theme"})
    @Test(description = "verify that create EDC Bank EMI link and validate from theia_facade logs")
    public void Initiate_Txn_006(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.Edc_Emi.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","4600");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        String linkId = withDrawJson1.getString("body.linkId");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink,user);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardNoBox().sendKeys("4854980821814649");
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys("05");
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys("30");
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("123");
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        String log_bankEmi= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid,"BANK_EMI");
        Assert.assertNotNull(log_bankEmi);
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-57869")
    @Parameters({"theme"})
    @Test(description = "Verify card bin is passed in apply request 3/6/9 digits")
    public void cardBinInApplyReq(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","13800.0");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.AlternateID_VISA_CARD);
        String applyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"ads/v2/offer/apply","REQUEST");
        Assertions.assertThat(applyLogs).contains("\"bin6\":489538,\"bin8\":48953801,\"bin\":\"489538011\"");
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-57934")
    @Parameters({"theme"})
    @Test(description = "Verify subvention% in modify request ")
    public void subventionPercentInOrderModify(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","13800.0");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        String applyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ads/v2/offer/apply","RESPONSE");
        Assertions.assertThat(applyLogs).contains("\"value\":3.145");


        String modifyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_ORDER_MODIFY","REQUEST");
        Assertions.assertThat(modifyLogs).contains("\"subvention\":\"3.145\"");

    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PGP-58010")
    @Feature("PGP-58730")
    @Feature("PGP-58215")
    @Feature("PGP-59031")
    @Feature("PGP-60953")
    @Parameters({"theme"})
    @Test(description = "Verify additionalcashback% in modify request, create order request and order/checkout response / also verify phoneHash is being sent in offerApply request and orderCheckout request / also check if customer name is sent in acquiring pay order request")
    public void VerifyAdditionalCashbackPercentageForEdcBrandEMICaseForMultipleOffer(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","25738.62")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","1134.03")
                .setContext("body.edcEmiFields.ean","")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","MERCHANT")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2478081")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].amount.value","809.48")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerId","2478082")
                //.deleteContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1]")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","24487.69")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value","8432.39")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value","619.50")
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","3")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","15.0")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","1060.95")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","HDFC|3")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","307312565796316169")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","25297.17")
                .setContext("body.edcEmiFields.loanAmount","24677.67")
                .setContext("body.edcEmiFields.model","56002")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","56002")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.AlternateID_RUPAY_CARD); 
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        String CheckoutLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","RESPONSE");
        Assertions.assertThat(CheckoutLogs).contains("\"value\":4.22");
        Assertions.assertThat(CheckoutLogs).contains("\"value\":3.15");
        String modifyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_ORDER_MODIFY","REQUEST");
        Assertions.assertThat(modifyLogs).contains("\"additionalCashBack\":\"7.23\"");
        String createLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_CREATE_ORDER","REQUEST");
        Assertions.assertThat(createLogs).contains("\"additionalCashBack\":\"7.23\"");
        //phone hash assertions
        
        String CheckoutRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","REQUEST");
        System.out.println("CheckoutRequestLogs: "+ CheckoutRequestLogs);
        JsonPath CheckoutRequestLogsjson = new JsonPath(CheckoutRequestLogs.substring(CheckoutRequestLogs.indexOf("entity=")+7,CheckoutRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("CheckoutRequestLogsjson: "+CheckoutRequestLogsjson.prettyPrint());
        Assertions.assertThat(CheckoutRequestLogsjson.getString("userInfo.phoneHash")).isNotEmpty();
        
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        JsonPath OfferApplyRequestLogsjson = new JsonPath(OfferApplyRequestLogs.substring(OfferApplyRequestLogs.indexOf("entity=")+7,OfferApplyRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("OfferApplyRequestLogsjson: "+OfferApplyRequestLogsjson.prettyPrint());
        Assertions.assertThat(OfferApplyRequestLogsjson.getString("userDetails.phoneHash")).isNotBlank();
        Assertions.assertThat(OfferApplyRequestLogsjson.getInt("paymentDetails.originalAmount")).isEqualTo(2687265);
        Assertions.assertThat(OfferApplyRequestLogsjson.getDouble("items[0].price")).isEqualTo(2687265.0);
        
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}")   // Fix object boundaries
                .replace("\"[", "[")   // Fix object boundaries
                .replace("]\"", "]");  // Fix object boundaries
        JsonPath jsonPath = new JsonPath(cleanedJson);
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("originalCardHash:9fff5b365aa8e3934355eaccf00604a26833423301424bdb34914648855a214c");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:9fff5b365aa8e3934355eaccf00604a26833423301424bdb34914648855a214c");
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("emiInterestRate:15.0");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("emiInterestRate:15.0");
         // AI-Generated: 2025-09-11 - Logic implementation
         int cardHolderNameCount = (payLog.split("cardHolderName", -1).length) - 1;
         Assert.assertEquals(cardHolderNameCount, 3, "cardHolderName should appear exactly 3 times in ACQUIRING_PAY_ORDER request logs");
         
         // AI-Generated: 2025-09-11 - Logic implementation
         String theiaLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/acquiring/payOrder");
         Assertions.assertThat(theiaLogs).contains("\"cardHolderName\":\"ABC abc\"");
         
    }

    @Owner("Lokesh Saxena")
    @Feature("PGP-58010")
    @Feature("PGP-58730")
    @Feature("PGP-58215")
    @Feature("PGP-59031")
    @Feature("PGP-60953")
    @Parameters({"theme"})
    @Test(description = "Verify additionalcashback% in modify request, create order request and order/checkout response /  also check if customer name is sent in acquiring pay order request")
    public void VerifyAdditionalCashbackPercentageForEdcBankEMICaseForSingleOffer(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29);
        String applyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","RESPONSE");
        Assertions.assertThat(applyLogs).contains("\"value\":4.22");
        String modifyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_ORDER_MODIFY","REQUEST");
        Assertions.assertThat(modifyLogs).contains("\"additionalCashBack\":\"4.22\"");
        String createLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_CREATE_ORDER","REQUEST");
        Assertions.assertThat(createLogs).contains("\"additionalCashBack\":\"4.22\"");

        String CheckoutRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","REQUEST");
        System.out.println("CheckoutRequestLogs: "+ CheckoutRequestLogs);
        JsonPath CheckoutRequestLogsjson = new JsonPath(CheckoutRequestLogs.substring(CheckoutRequestLogs.indexOf("entity=")+7,CheckoutRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("CheckoutRequestLogsjson: "+CheckoutRequestLogsjson.prettyPrint());
        Assertions.assertThat(CheckoutRequestLogsjson.getString("userInfo.phoneHash")).isNotEmpty();
        
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        JsonPath OfferApplyRequestLogsjson = new JsonPath(OfferApplyRequestLogs.substring(OfferApplyRequestLogs.indexOf("entity=")+7,OfferApplyRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("OfferApplyRequestLogsjson: "+OfferApplyRequestLogsjson.prettyPrint());
        Assertions.assertThat(OfferApplyRequestLogsjson.getString("userDetails.phoneHash")).isNotBlank();
        Assertions.assertThat(OfferApplyRequestLogsjson.getInt("paymentDetails.originalAmount")).isEqualTo(2687265);
        Assertions.assertThat(OfferApplyRequestLogsjson.getDouble("items[0].price")).isEqualTo(2687265.0);

        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}")   // Fix object boundaries
                .replace("\"[", "[")   // Fix object boundaries
                .replace("]\"", "]");  // Fix object boundaries
        JsonPath jsonPath = new JsonPath(cleanedJson);
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("originalCardHash:6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58");
        
        // AI-Generated: 2025-09-11 - Logic implementation
        int cardHolderNameCount = (payLog.split("cardHolderName", -1).length) - 1;
        Assert.assertEquals(cardHolderNameCount, 3, "cardHolderName should appear exactly 3 times in ACQUIRING_PAY_ORDER request logs");
        
        // AI-Generated: 2025-09-11 - Logic implementation
        String theiaLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/acquiring/payOrder");
        Assertions.assertThat(theiaLogs).contains("\"cardHolderName\":\"ABC abc\"");
        
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PGP-58171")
    @Feature("PGP-58730")
    @Parameters({"theme"})
    @Test(description = "Verify originalCardHash field should be passed into ACQUIRING_PAY_ORDER request logs in facade for standardFlow(edcEmiFields = null)")
    public void verifyOriginalCardHashForStandardFlowUsingVISACardScheme(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId(), "", "EMI")
                .setContext("body.amount","13800.0")
                .deleteContext("body.edcEmiFields");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkNew(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String modifyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"LI_"+withDrawJson1.getString("body.linkId"),"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = modifyLogs.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}")   // Fix object boundaries
                .replace("\"[", "[")   // Fix object boundaries
                .replace("]\"", "]");  // Fix object boundaries
        JsonPath jsonPath = new JsonPath(cleanedJson);
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("originalCardHash:6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58");
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PGP-58171")
    @Feature("PGP-58730")
    @Parameters({"theme"})
    @Test(description = "Verify originalCardHash field should be passed into ACQUIRING_PAY_ORDER request facade logs for standard flow for rupay card")
    public void verifyOriginalCardHashForStandardEMIUsingRupayCardScheme(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","13800.0")
                .deleteContext("body.edcEmiFields");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkNew(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        PaymentDTO paymentDTO =new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.RUPAY_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        String modifyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"LI_"+withDrawJson1.getString("body.linkId"),"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = modifyLogs.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}")   // Fix object boundaries
                .replace("\"[", "[")   // Fix object boundaries
                .replace("]\"", "]");  // Fix object boundaries
        JsonPath jsonPath = new JsonPath(cleanedJson);
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("originalCardHash:711c56c7640eec11e0f91af0fa58c2e199887527c60a3d21ca0dda064f51a21c");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:711c56c7640eec11e0f91af0fa58c2e199887527c60a3d21ca0dda064f51a21c");
    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60953")
    @Feature("PGP-60757")
    @Parameters({"theme"})
    @Test(description = "Verify BANK FS with EMI months tenure as 0, plan ID as 123, and EMI amount as null, also verify customer name in ACQUIRING_PAY_ORDER request")
    public void VerifyBankFS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiMonths","0");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.planId","123");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiAmount",null);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkFS(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-09-11 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPayFS(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29);
        String applyLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","RESPONSE");
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(applyLogs).contains("\"value\":4.22");

        String CheckoutRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","REQUEST");
        System.out.println("CheckoutRequestLogs: "+ CheckoutRequestLogs);
        JsonPath CheckoutRequestLogsjson = new JsonPath(CheckoutRequestLogs.substring(CheckoutRequestLogs.indexOf("entity=")+7,CheckoutRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("CheckoutRequestLogsjson: "+CheckoutRequestLogsjson.prettyPrint());
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(CheckoutRequestLogsjson.getString("userInfo.phoneHash")).isNotEmpty();
        
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        JsonPath OfferApplyRequestLogsjson = new JsonPath(OfferApplyRequestLogs.substring(OfferApplyRequestLogs.indexOf("entity=")+7,OfferApplyRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("OfferApplyRequestLogsjson: "+OfferApplyRequestLogsjson.prettyPrint());
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(OfferApplyRequestLogsjson.getString("userDetails.phoneHash")).isNotBlank();
        softAssert.assertThat(OfferApplyRequestLogsjson.getInt("paymentDetails.originalAmount")).isEqualTo(2687265);
        softAssert.assertThat(OfferApplyRequestLogsjson.getDouble("items[0].price")).isEqualTo(2687265.0);

        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER","REQUEST");
        
        // AI-Generated: 2025-09-11 - Logic implementation
        int cardHolderNameCount = (payLog.split("cardHolderName", -1).length) - 1;
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(cardHolderNameCount).isEqualTo(3).withFailMessage("cardHolderName should appear exactly 3 times in ACQUIRING_PAY_ORDER request logs");
        
        // AI-Generated: 2025-09-11 - Logic implementation
        String theiaLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/acquiring/payOrder");
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(theiaLogs).contains("\"cardHolderName\":\"ABC abc\"");
        
        // AI-Generated: 2025-09-24 - Soft assertion finalization
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60953")
    @Feature("PGP-60757")
    @Parameters({"theme"})
    @Test(description = "Verify BRAND FS with EMI months tenure as 0, plan ID as 123, and EMI amount as null, also verify customer name in ACQUIRING_PAY_ORDER request")
    public void VerifyBrandFS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","25738.62")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","1134.03")
                .setContext("body.edcEmiFields.ean","")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","MERCHANT")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2478081")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].amount.value","809.48")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerId","2478082")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","24487.69")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount","null")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount",null)
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","0")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","0")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","1060.95")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","HDFC|0")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","123")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","25297.17")
                .setContext("body.edcEmiFields.loanAmount",null)
                .setContext("body.edcEmiFields.model","56002")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","56002")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkFS(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPayFS(PaymentDTO.AlternateID_RUPAY_CARD); 
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        String CheckoutLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","RESPONSE");
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(CheckoutLogs).contains("\"value\":4.22");
        softAssert.assertThat(CheckoutLogs).contains("\"value\":3.15");
        
        String CheckoutRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","REQUEST");
        System.out.println("CheckoutRequestLogs: "+ CheckoutRequestLogs);
        JsonPath CheckoutRequestLogsjson = new JsonPath(CheckoutRequestLogs.substring(CheckoutRequestLogs.indexOf("entity=")+7,CheckoutRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("CheckoutRequestLogsjson: "+CheckoutRequestLogsjson.prettyPrint());
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(CheckoutRequestLogsjson.getString("userInfo.phoneHash")).isNotEmpty();
        
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        JsonPath OfferApplyRequestLogsjson = new JsonPath(OfferApplyRequestLogs.substring(OfferApplyRequestLogs.indexOf("entity=")+7,OfferApplyRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("OfferApplyRequestLogsjson: "+OfferApplyRequestLogsjson.prettyPrint());
        // AI-Generated: 2025-09-24 - Soft assertion conversion
        softAssert.assertThat(OfferApplyRequestLogsjson.getString("userDetails.phoneHash")).isNotBlank();
        softAssert.assertThat(OfferApplyRequestLogsjson.getInt("paymentDetails.originalAmount")).isEqualTo(2687265);
        softAssert.assertThat(OfferApplyRequestLogsjson.getDouble("items[0].price")).isEqualTo(2687265.0);
        
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER","REQUEST");
         // AI-Generated: 2025-09-11 - Logic implementation
         int cardHolderNameCount = (payLog.split("cardHolderName", -1).length) - 1;
         // AI-Generated: 2025-09-24 - Soft assertion conversion
         softAssert.assertThat(cardHolderNameCount).isEqualTo(3).withFailMessage("cardHolderName should appear exactly 3 times in ACQUIRING_PAY_ORDER request logs");
         
         // AI-Generated: 2025-09-11 - Logic implementation
         String theiaLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/acquiring/payOrder");
         // AI-Generated: 2025-09-24 - Soft assertion conversion
         softAssert.assertThat(theiaLogs).contains("\"cardHolderName\":\"ABC abc\"");
         
         // AI-Generated: 2025-09-24 - Soft assertion finalization
         softAssert.assertAll();
    }


    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60953")
    @Parameters({"theme"})
    @Test(description = "Verify Error Msg When Customer Name Is Empty")
    public void ValidateErrorMsgWhenCustomerNameIsEmpty(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String expectedErrorMessage = "Please enter valid card holder name";
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","13800.0");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Payment Details
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        PaymentDTO paymentDTO =new PaymentDTO();
        linkPaymentLoginPage.EdcCardNoBox().sendKeys(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys(paymentDTO.getExpMonth());
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys(paymentDTO.getExpYear().substring(2));
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        linkPaymentLoginPage.EDCLinkPayButton().click();
        // AI-Generated: 2025-09-10 - Logic implementation
        DriverManager.getDriver().switchTo().frame(0);
        String actualErrorMessage = cashierPage.invalidCardholderNameText().getText();
        Assert.assertEquals(actualErrorMessage, expectedErrorMessage);
    }


    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PGP-61166")
    @Feature("PGP-61165")
    @Parameters({"theme"})
    @Test(description = "Verify Store Page Bank Emi Case")
    public void StorePageBankEmiCase(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","26842.65");
        // AI-Generated: 2025-10-16 - Logic implementation
        // Create transactionalInfo object with affordabilitySource
        Map<String, Object> transactionalInfo = new HashMap<>();
        transactionalInfo.put("affordabilitySource", "STORE_PAGE");

        createNewLink
            .setContext("body.customerContact.customerMobile", mobileNumber)
            .setContext("body.transactionalInfo", transactionalInfo)
            .setContext("body.edcEmiFields.bankCode", "ICICI")
            .setContext("body.edcEmiFields.bankName", "ICICI Bank")
            .setContext("body.edcEmiFields.emiChannelDetail.emiMonths", "6")
            .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId", "ICICI|6")
            .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId", "2494271")
            .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value", "30.00")
            .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value", "28109.16")
            .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value", "4684.86")
            .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value", "1266.51")
            .setContext("body.edcEmiFields.emiChannelDetail.interestRate", "16.0")
            .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value", "28109.16")
            .setContext("body.edcEmiFields.emiChannelDetail.planId", "410034883492884481")
            .setContext("body.edcEmiFields.productAmount", "26872.65")
            .setContext("body.edcEmiFields.loanAmount", "26842.65");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.StorePageFillEdcCardDetailsAndPay(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);

        // AI-Generated: 2025-10-22 - Logic implementation
        // Extract orderId from logs after payment is initiated
        String paymentConsultLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, withDrawJson1.getString("body.linkId"), "link/paymentConsult", "REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29);

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify metadata.affordabilitySource in initiate transaction logs
        String initiateTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "/theia/api/v1/initiateTransaction", "REQUEST");
        softAssert.assertThat(initiateTxnLogs).contains("\"affordabilitySource\":\"STORE_PAGE\"");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify source header in offer apply logs
        String offerApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ads/v2/offer/apply", "REQUEST");
        softAssert.assertThat(offerApplyLogs).contains("source=[STORE_PAGE]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify bankAcquirers in offer apply response
        String offerApplyResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ads/v2/offer/apply", "RESPONSE");
        softAssert.assertThat(offerApplyResponseLogs).contains("\"bankAcquirers\":[\"ICPP\",\"ICED\"]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify X-Channel header in order checkout logs
        String orderCheckoutLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ats/v2/order/checkout", "REQUEST");
        softAssert.assertThat(orderCheckoutLogs).contains("X-Channel=[STORE_PAGE]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify bankAcquirers in order checkout response
        String orderCheckoutResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ats/v2/order/checkout", "RESPONSE");
        softAssert.assertThat(orderCheckoutResponseLogs).contains("\"bankAcquirers\":[\"ICPP\",\"ICED\"]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify hardwhitelist parameter in ACQUIRING_PAY_ORDER logs
        String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_PAY_ORDER", "REQUEST");
        softAssert.assertThat(acquiringPayOrderLogs).contains("\"hardwhitelist\":\"ICPP,ICED\"");

        // AI-Generated: 2025-10-22 - Soft assertion finalization
        softAssert.assertAll();
    }


    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PGP-61166")
    @Feature("PGP-61165")
    @Parameters({"theme"})
    @Test(description = "Verify Store Page Brand Emi Case")
    public void StorePageBrandEmiCase(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        // AI-Generated: 2025-10-17 - Logic implementation
        // Create transactionalInfo object with affordabilitySource
        Map<String, Object> transactionalInfo = new HashMap<>();
        transactionalInfo.put("affordabilitySource", "STORE_PAGE");

        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.amount","19929.49")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.transactionalInfo", transactionalInfo)
                .setContext("body.edcEmiFields.bankCode", "ICICI")
                .setContext("body.edcEmiFields.bankName", "ICICI Bank")
                .setContext("body.edcEmiFields.model","model17")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","1234586283")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS")
                .setContext("body.edcEmiFields.validationValue", "55667778888")
                .setContext("body.edcEmiFields.loanAmount","14947.12")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","300.00")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2494269")
                .deleteContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1]")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","20869.80")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value","2647.91")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value","940.31")
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","6")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","16.0")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","ICICI|6")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","410034883492884481")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","20869.80")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","6643.16")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerId","2494270")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].type","DISCOUNT");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.StorePageFillEdcCardDetailsAndPay(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);

        // AI-Generated: 2025-10-22 - Logic implementation
        // Extract orderId from logs after payment is initiated
        String paymentConsultLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, withDrawJson1.getString("body.linkId"), "link/paymentConsult", "REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29);

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify metadata.affordabilitySource in initiate transaction logs
        String initiateTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "/theia/api/v1/initiateTransaction", "REQUEST");
        softAssert.assertThat(initiateTxnLogs).contains("\"affordabilitySource\":\"STORE_PAGE\"");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify source header in offer apply logs
        String offerApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ads/v2/offer/apply", "REQUEST");
        softAssert.assertThat(offerApplyLogs).contains("source=[STORE_PAGE]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify bankAcquirers in offer apply response (Brand EMI has more acquirers)
        String offerApplyResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ads/v2/offer/apply", "RESPONSE");
        softAssert.assertThat(offerApplyResponseLogs).contains("\"bankAcquirers\":[\"ICPP\",\"RBDC\",\"ICED\",\"HEDC\"]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify X-Channel header in order checkout logs
        String orderCheckoutLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ats/v2/order/checkout", "REQUEST");
        softAssert.assertThat(orderCheckoutLogs).contains("X-Channel=[STORE_PAGE]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify bankAcquirers in order checkout response (Brand EMI has more acquirers)
        String orderCheckoutResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ats/v2/order/checkout", "RESPONSE");
        softAssert.assertThat(orderCheckoutResponseLogs).contains("\"bankAcquirers\":[\"ICPP\",\"RBDC\",\"ICED\",\"HEDC\"]");

        // AI-Generated: 2025-10-22 - Soft assertion conversion
        // Verify hardwhitelist parameter in ACQUIRING_PAY_ORDER logs (Brand EMI has more acquirers)
        String acquiringPayOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_PAY_ORDER", "REQUEST");
        softAssert.assertThat(acquiringPayOrderLogs).contains("\"hardwhitelist\":\"ICPP,RBDC,ICED,HEDC\"");

        // AI-Generated: 2025-10-22 - Soft assertion finalization
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-59058")
    @Parameters({"theme"})
    @Test(description = "Verify Error Message for ROUTING_FAILED")
    public void VerifyErrorMessageFor_ROUTING_FAILED(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EDC_INSTA_ERROR_MID.getId().toString());
        // Taking random new mobile number each time
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.cardType","CREDIT_CARD");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiMonths","0");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.planId","123");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiAmount",null);
        createNewLink.setContext("body.edcEmiFields.loanAmount",null);

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkFS(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-10-15 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPayFS(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        
        String log = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"PAYMENT_BIZ_PAY_RESULT_QUERY","RESPONSE");
         // AI-Generated: 2025-10-15 - Soft assertion conversion
        softAssert.assertThat(log).contains("\"instErrorCode\":\"ROUTING_FAILED\"");

        String Theia_log = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,orderId,"v1/processTransaction","RESPONSE");         
        // AI-Generated: 2025-10-15 - JSON parsing and assertion
        String jsonPayload = Theia_log.substring(Theia_log.indexOf("\"PAYLOAD\" : ") + 12);
        String cleanedJson = jsonPayload.strip()
                 .replace("\\\"", "\"") // Replace escaped quotes
                 .replace("\"{", "{")   // Fix object boundaries
                 .replace("}\"", "}")   // Fix object boundaries
                 .replace("\"[", "[")   // Fix object boundaries
                 .replace("]\"", "]");  // Fix object boundaries
        JsonPath theiaJsonPath = new JsonPath(cleanedJson);
        // AI-Generated: 2025-10-15 - Soft assertion conversion
        softAssert.assertThat(theiaJsonPath.getString("body.txnInfo.RESPMSG")).isEqualTo("Transaction failed due to merchant configuration issue at Paytm.");

        softAssert.assertAll();
    }

    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-59058")
    @Parameters({"theme"})
    @Test(description = "Verify Error Message for AFFORDABILITY_ISSUE")
    public void VerifyErrorMessageFor_AFFORDABILITY_ISSUE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.cardType","DEBIT_CARD");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiMonths","0");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.planId","123");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiAmount",null);
        createNewLink.setContext("body.edcEmiFields.loanAmount",null);

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkFS(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-10-15 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPayFS("4444333327624111");
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        System.out.println("orderId: "+orderId);

        String log = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "PAYMENT_BIZ_PAY_RESULT_QUERY", "RESPONSE");
        softAssert.assertThat(log).contains("\"instErrorCode\":\"AFFORDABILITY_ISSUE\"");

        String Theia_log = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "v1/processTransaction", "RESPONSE");
        String jsonPayload = Theia_log.substring(Theia_log.indexOf("\"PAYLOAD\" : ") + 12);
        String cleanedJson = jsonPayload.strip()
                .replace("\\\"", "\"")
                .replace("\"{", "{")
                .replace("}\"", "}")
                .replace("\"[", "[")
                .replace("]\"", "]");
        JsonPath theiaJsonPath = new JsonPath(cleanedJson);
        softAssert.assertThat(theiaJsonPath.getString("body.txnInfo.RESPMSG")).containsIgnoringCase("affordability");

        softAssert.assertAll();
    }
    
    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-61357")
    @Parameters({"theme"})
    @Test(description = "Verify UI on Accordian Click")
    public void verifyUIonAccordianClick(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        
        // Create transactionalInfo object with affordabilitySource
        Map<String, Object> transactionalInfo = new HashMap<>();
        transactionalInfo.put("affordabilitySource", "STORE_PAGE");

        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid)
                .setContext("body.amount","19929.49")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.transactionalInfo", transactionalInfo)
                .setContext("body.edcEmiFields.bankCode", "ICICI")
                .setContext("body.edcEmiFields.bankName", "ICICI Bank")
                .setContext("body.edcEmiFields.model","model17")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","1234586283")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS")
                .setContext("body.edcEmiFields.validationValue", "55667778888")
                .setContext("body.edcEmiFields.loanAmount","14947.12")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","300.00")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2494269")
                .deleteContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1]")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","20869.80")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value","2647.91")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value","940.31")
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","6")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","16.0")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","ICICI|6")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","410034883492884481")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","20869.80")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","6643.16")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerId","2494270")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].type","DISCOUNT");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        

        linkPaymentLoginPage.clickCardNumberTabCenter();
        DriverManager.getDriver().switchTo().frame(0);
        linkPaymentLoginPage.EdcCardHolderNameBox().sendKeys("ABC abc");
        linkPaymentLoginPage.EdcCardNoBox().assertVisible();

        
    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60892")
    @Parameters({"theme"})
    @Test(description = "Verify flowType = Brand is being floated in initiate and offerApply")
    public void VerifyFlowTypeBrandIsBeingFloatedInInitiateAndOfferApply(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        String flowType = "BRAND";
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","25738.62")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.edcEmiFields.flowType", flowType)
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","1134.03")
                .setContext("body.edcEmiFields.ean","")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","MERCHANT")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2478081")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].amount.value","809.48")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerId","2478082")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","24487.69")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value","8432.39")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value","619.50")
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","3")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","15.0")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","1060.95")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","HDFC|3")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","307312565796316169")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","25297.17")
                .setContext("body.edcEmiFields.loanAmount","24677.67")
                .setContext("body.edcEmiFields.model","56002")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","56002")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.AlternateID_RUPAY_CARD); 
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        
        // Verify flowType in initiate transaction logs
        String initiateTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "/theia/api/v1/initiateTransaction", "REQUEST");
        softAssert.assertThat(initiateTxnLogs).contains("\"flowType\":\"BRAND\"");
         
        // Verify flowType in offer apply logs
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        softAssert.assertThat(OfferApplyRequestLogs).contains("flowType=[BRAND]");

        softAssert.assertAll();
    }

    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60892")
    @Parameters({"theme"})
    @Test(description = "Verify flowType = Bank is being floated in initiate and offerApply")
    public void VerifyFlowTypeBankIsBeingFloatedInInitiateAndOfferApply(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        String flowType = "BANK";
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.flowType", flowType);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        softAssert.assertThat(paymentConsultLogs).isNotEmpty().withFailMessage("paymentConsultLogs should not be empty");
        int orderIdStartIndex = paymentConsultLogs.indexOf("ORDER_ID\":\"");
        softAssert.assertThat(orderIdStartIndex).isGreaterThanOrEqualTo(0).withFailMessage("ORDER_ID not found in paymentConsultLogs");
        String orderId = paymentConsultLogs.substring(orderIdStartIndex + 11, orderIdStartIndex + 29).replace("\"","");
        
        // Verify flowType in initiate transaction logs
        String initiateTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "/theia/api/v1/initiateTransaction", "REQUEST");
        softAssert.assertThat(initiateTxnLogs).contains("\"flowType\":\"BANK\"");
        
        // Verify flowType in offer apply logs
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        softAssert.assertThat(OfferApplyRequestLogs).contains("flowType=[BANK]");

        softAssert.assertAll();
    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60892")
    @Parameters({"theme"})
    @Test(description = "Verify error message when flowType = INVALID is being passed for Bank EMI")
    public void VerifyErrorWhenFlowTypeInvalidIsBeingPassed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        String flowType = "INVALID";
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.flowType", flowType);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        softAssert.assertThat(paymentConsultLogs).isNotEmpty().withFailMessage("paymentConsultLogs should not be empty");
        int orderIdStartIndex = paymentConsultLogs.indexOf("ORDER_ID\":\"");
        softAssert.assertThat(orderIdStartIndex).isGreaterThanOrEqualTo(0).withFailMessage("ORDER_ID not found in paymentConsultLogs");
        String orderId = paymentConsultLogs.substring(orderIdStartIndex + 11, orderIdStartIndex + 29).replace("\"","");
        
        // Verify flowType in initiate transaction logs
        String initiateTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "/theia/api/v1/initiateTransaction", "REQUEST");
        softAssert.assertThat(initiateTxnLogs).contains("\"flowType\":\"INVALID\"");
        
        // Verify flowType in offer apply logs
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        softAssert.assertThat(OfferApplyRequestLogs).contains("flowType=[INVALID]");
        
        // Verify error message in offer apply response logs
        String OfferApplyResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "/ads/v2/offer/apply", "RESPONSE");
        softAssert.assertThat(OfferApplyResponseLogs).contains("\"status\":\"FAILURE\"");
        softAssert.assertThat(OfferApplyResponseLogs).contains("\"statusCode\":\"E0001\"");
        softAssert.assertThat(OfferApplyResponseLogs).contains("allowed values for flowType are [BANK, RETAILER, AMOUNT_BASED_OFFERS, BRAND, AMOUNT_BASED_SUBVENTION]");
        
        softAssert.assertAll();
    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60892")
    @Parameters({"theme"})
    @Test(description = "Verify flowType blank is being is being passed for Bank EMI")
    public void VerifyWhenFlowTypeBlankIsBeingPassed(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        String flowType = "";
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","25738.62");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.flowType", flowType);
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2025-09-10 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPay(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        softAssert.assertThat(paymentConsultLogs).isNotEmpty().withFailMessage("paymentConsultLogs should not be empty");
        int orderIdStartIndex = paymentConsultLogs.indexOf("ORDER_ID\":\"");
        softAssert.assertThat(orderIdStartIndex).isGreaterThanOrEqualTo(0).withFailMessage("ORDER_ID not found in paymentConsultLogs");
        String orderId = paymentConsultLogs.substring(orderIdStartIndex + 11, orderIdStartIndex + 29).replace("\"","");
        
        // Verify flowType in initiate transaction logs
        String initiateTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, orderId, "/theia/api/v1/initiateTransaction", "REQUEST");
        softAssert.assertThat(initiateTxnLogs).contains("\"flowType\":\"\"");
        
        // Verify flowType in offer apply logs
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        softAssert.assertThat(OfferApplyRequestLogs).contains("flowType=[RETAILER]");
        
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-1232")
    @Parameters({"theme"})
    @Test(description = "Verify customer name validation for Brand FS")
    public void VerifyBrandFSForCustomerNameValidation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","25738.62")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","1134.03")
                .setContext("body.edcEmiFields.ean","")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","MERCHANT")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2478081")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].amount.value","809.48")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerId","2478082")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","24487.69")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount","null")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount",null)
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","0")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","0")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","1060.95")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","HDFC|0")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","123")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","25297.17")
                .setContext("body.edcEmiFields.loanAmount",null)
                .setContext("body.edcEmiFields.model","56002")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","56002")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkFS(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
     //   linkPaymentLoginPage.fillEdcCardDetailsAndPayFS(PaymentDTO.AlternateID_RUPAY_CARD); 
        linkPaymentLoginPage.EdcCardEnter().click();
     // AI-Generated: 2025-09-18 - Logic implementation
     // Try iframe switch for FS version as well
        DriverManager.getDriver().switchTo().frame(1);
        PaymentDTO paymentDTO = new PaymentDTO();
        linkPaymentLoginPage.EdcCardHolderNameBox().sendKeys("AB");
        linkPaymentLoginPage.EnterCardHolderNameText().assertVisible(); // Min length is 3, 2 chars should show error
        linkPaymentLoginPage.EdcCardNoBoxFS().sendKeys("4718650100010336");
        linkPaymentLoginPage.EdcCardExpiryMonthBoxFS().sendKeys(paymentDTO.getExpMonth());
        linkPaymentLoginPage.EdcCardExpiryYearBoxFS().sendKeys(paymentDTO.getExpYear().substring(2));
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys("567");
        DriverManager.getDriver().switchTo().defaultContent();
        Thread.sleep(2000);
        linkPaymentLoginPage.EDCLinkPayButton().click();
        DriverManager.getDriver().switchTo().frame(0);
        String expectedErrorMessage = "Please enter a valid name";
        String actualErrorMessage = cashierPage.invalidCardholderNameText().getText();
        softAssert.assertThat(actualErrorMessage).as("Min length (3 chars): card holder name with 2 chars should show error").isEqualTo(expectedErrorMessage);
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.LOKESH_SAXENA)
    @Feature("PG-1232")
    @Parameters({"theme"})
    @Test(description = "Brand Emi Case Customer Name Validation")
    public void BrandEmiCaseCustomerNameValidation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        // AI-Generated: 2025-10-17 - Logic implementation
        // Create transactionalInfo object with affordabilitySource
        Map<String, Object> transactionalInfo = new HashMap<>();
        transactionalInfo.put("affordabilitySource", "STORE_PAGE");

        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.amount","19929.49")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.transactionalInfo", transactionalInfo)
                .setContext("body.edcEmiFields.bankCode", "ICICI")
                .setContext("body.edcEmiFields.bankName", "ICICI Bank")
                .setContext("body.edcEmiFields.model","model17")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","26872.65")
                .setContext("body.edcEmiFields.productId","1234586283")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS")
                .setContext("body.edcEmiFields.validationValue", "55667778888")
                .setContext("body.edcEmiFields.loanAmount","14947.12")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","300.00")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2494269")
                .deleteContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1]")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","20869.80")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value","2647.91")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value","940.31")
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","6")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","16.0")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","ICICI|6")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","410034883492884481")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","20869.80")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","6643.16")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerId","2494270")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].type","DISCOUNT");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2025-09-10 - Refactoring
        //    linkPaymentLoginPage.StorePageFillEdcCardDetailsAndPay(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        linkPaymentLoginPage.EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        PaymentDTO paymentDTO = new PaymentDTO();
        linkPaymentLoginPage.EnterCardHolderNameText().assertVisible(); 
        linkPaymentLoginPage.EdcCardHolderNameBox().sendKeys("AB");
        linkPaymentLoginPage.EdcCardNoBox().sendKeys(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        linkPaymentLoginPage.EdcCardExpiryMonthBox().sendKeys(paymentDTO.getExpMonth());
        linkPaymentLoginPage.EdcCardExpiryYearBox().sendKeys(paymentDTO.getExpYear().substring(2));
        linkPaymentLoginPage.EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        Thread.sleep(2000);
        linkPaymentLoginPage.EDCLinkPayButton().click();
        DriverManager.getDriver().switchTo().frame(0);
        String expectedErrorMessage = "Please enter a valid name";
        String actualErrorMessage = cashierPage.invalidCardholderNameText().getText();
        softAssert.assertThat(actualErrorMessage).as("Min length (3 chars): card holder name with 2 chars should show error").isEqualTo(expectedErrorMessage);
        softAssert.assertAll();
    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-61394")
    @Parameters({"theme"})
    @Test(description = "Verify Apply Error Message")
    public void VerifyApplyErrorMessage(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID_NEW.getId().toString());
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","27.26");
        createNewLink.setContext("body.customerContact.customerMobile",mobileNumber);
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiMonths","0");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.planId","123");
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.emiAmount",null);
        createNewLink.setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","27.26");
        createNewLink.setContext("body.edcEmiFields.productAmount","27.26");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkFS(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.waitUntilLoads();
        // AI-Generated: 2026-04-24 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPayFS(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29);        
        String OfferApplyRequestLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","REQUEST");
        JsonPath OfferApplyRequestLogsjson = new JsonPath(OfferApplyRequestLogs.substring(OfferApplyRequestLogs.indexOf("entity=")+7,OfferApplyRequestLogs.indexOf("target=https")).strip().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}").replace("\"[", "[").replace("]\"", "]"));
        System.out.println("OfferApplyRequestLogsjson: "+OfferApplyRequestLogsjson.prettyPrint());

        String OfferApplyResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"/ads/v2/offer/apply","RESPONSE");
        System.out.println("OfferApplyResponseLogs: "+OfferApplyResponseLogs);
        softAssert.assertThat(OfferApplyResponseLogs).contains("E2007");
        softAssert.assertThat(OfferApplyResponseLogs).contains("No offer available");

        String theiaProcessTxnLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,orderId,"v1/processTransaction","RESPONSE");
        System.out.println("theiaProcessTxnResponse: "+theiaProcessTxnLog);
        softAssert.assertThat(theiaProcessTxnLog).contains("Offer changed or offer not applicable on entered card.");
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PG-4424")
    @Parameters({"theme"})
    @Test(description = "Verify param emi detail info in COP of standard links")
    public void verifyParamStandardLinks(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        setUserAndMId(Constants.MerchantType.EMI_EDC_LINK_MID.getId().toString());
        CreateNewLink createNewLink = new CreateNewLink(true,false).buildRequest(mid,"FIXED","27.26");
        createNewLink.deleteContext("body.edcEmiFields");

        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLinkNew(paymentLink);
        linkPaymentLoginPage.edcCustomerNameTextBox().sendKeys("abc");
        linkPaymentLoginPage.edcMobileNoTextBox().sendKeys("7480053111");
        linkPaymentLoginPage.edcEmailIdTextBox().sendKeys("puspa@gmail.com");
        linkPaymentLoginPage.buttonProceedToPayWithAmount().click();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO, false);
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29);
        String acquiringCreateOrderAndPayRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        softAssert.assertThat(acquiringCreateOrderAndPayRequestLogs)
                .as("detailExtendInfo EMI_DETAIL_INFO in COP")
                .contains("detailExtendInfo\":{\"EMI_DETAIL_INFO\":\"{\"emiAmount\":\"913\",\"loanAmount\":\"2726\"}\"");
        softAssert.assertThat(acquiringCreateOrderAndPayRequestLogs).as("extendInfo emiInterestRate in COP").contains("\"emiInterestRate\":\"3.0\"");
        softAssert.assertThat(acquiringCreateOrderAndPayRequestLogs).as("extendInfo emiTenure in COP").contains("\"emiTenure\":\"3\"");
        softAssert.assertThat(acquiringCreateOrderAndPayRequestLogs).as("extendInfo emiPlanId in COP").contains("\"emiPlanId\":\"HDFC|3\"");
        softAssert.assertAll();
    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PG-6375")
    @Parameters({"theme"})
        @Test(description = "Verify success EDC link txn for PCF and MDR merchant")
        public void VerifySucessEdcLinkTxnForPcfAndMdrMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        SoftAssertions softAssert = new SoftAssertions();
        Constants.MerchantType mid = Constants.MerchantType.PLE_DEALS_MID;
        String mobileNumber = "9" + String.format("%09d", new Random().nextInt(1000000000));
        CreateNewLink createNewLink = (CreateNewLink) new CreateNewLink(true,"")
                .setContext("body.mid", mid.getId())
                .setContext("body.amount","6790.00")
                .setContext("body.customerContact.customerMobile",mobileNumber)
                .setContext("body.edcEmiFields.ean","")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].amount.value","3000.00")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].offerId","2408209")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[0].type","discount")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].amount.value","210.00")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerContributorType","MERCHANT")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].offerId","2195821")
                .setContext("body.edcEmiFields.emiChannelDetail.bankOfferDetails[1].type","discount")
                .setContext("body.edcEmiFields.emiChannelDetail.effectiveAmount.value","6629.82")
                .setContext("body.edcEmiFields.emiChannelDetail.emiAmount.value","2209.94")
                .setContext("body.edcEmiFields.emiChannelDetail.interestAmount.value","162.35")
                .setContext("body.edcEmiFields.emiChannelDetail.emiMonths","3")
                .setContext("body.edcEmiFields.emiChannelDetail.interestRate","15.0")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].amount.value","322.53")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerContributorType","BRAND")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].offerId","2499765")
                .setContext("body.edcEmiFields.emiChannelDetail.offerDetails[0].type","DISCOUNT")
                .setContext("body.edcEmiFields.emiChannelDetail.pgPlanId","HDFC|3")
                .setContext("body.edcEmiFields.emiChannelDetail.planId","307312565796316169")
                .setContext("body.edcEmiFields.emiChannelDetail.totalAmount.value","27547.26")
                .setContext("body.edcEmiFields.loanAmount","6467.47")
                .setContext("body.edcEmiFields.model","sm-m315fzbgins")
                .setContext("body.edcEmiFields.productName","Test Product Apple_73")
                .setContext("body.edcEmiFields.productAmount","10000.00")
                .setContext("body.edcEmiFields.productId","1235528546")
                .setContext("body.edcEmiFields.productCode","51051000100000000101")
                .setContext("body.edcEmiFields.skuCode","SM-M315FZBGINS")
                .setContext("body.edcEmiFields.validationValue","352999112345678");
        JsonPath withDrawJson1 = createNewLink.execute().jsonPath();
        String paymentLink = withDrawJson1.getString("body.longUrl");
        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.OpenEdcLink(paymentLink);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        // AI-Generated: 2026-05-28 - Refactoring
        linkPaymentLoginPage.fillEdcCardDetailsAndPayNew(PaymentDTO.AlternateID_RUPAY_CARD); 
        String paymentConsultLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,withDrawJson1.getString("body.linkId"),"link/paymentConsult","REQUEST");
        String orderId = paymentConsultLogs.substring(paymentConsultLogs.indexOf("ORDER_ID\":\"") + 11, paymentConsultLogs.indexOf("ORDER_ID\":\"") + 29).replace("\"","");
        String checkoutRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ats/v2/order/checkout","REQUEST");
        JsonPath checkoutRequestJson = new JsonPath(checkoutRequestLogs.substring(checkoutRequestLogs.indexOf("entity=") + 7, checkoutRequestLogs.indexOf("target=https")).strip()
                .replace("\\\"", "\"")
                .replace("\"{", "{")
                .replace("}\"", "}")
                .replace("\"[", "[")
                .replace("]\"", "]"));
        softAssert.assertThat(checkoutRequestJson.getString("paymentInfo.paymentOptions[0].paymentDetails.cardCacheToken"))
                .as("cardCacheToken should be sent in order/checkout request paymentDetails and must not be null or empty")
                .isNotBlank();

        String feeBatchConsultLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId, "FEE_BATCH_CONSULT", "RESPONSE");
        System.out.println("FEE_BATCH_CONSULT logs: " + feeBatchConsultLogs);
        JsonPath feeBatchConsultJson = new JsonPath(feeBatchConsultLogs.substring(feeBatchConsultLogs.indexOf("\"RESPONSE\" : ") + 13, feeBatchConsultLogs.indexOf("\"TOTAL_TIME_TAKEN\"")).strip().replaceAll(",$", ""));
        softAssert.assertThat(feeBatchConsultJson.getString("consultResults[0].consultDetails[0].feeType"))
                .as("FEE_BATCH_CONSULT feeType should be PCF_ACQUIRING_SERVICE_FEE")
                .isEqualTo("PCF_ACQUIRING_SERVICE_FEE");
        softAssert.assertThat(feeBatchConsultJson.getInt("consultResults[0].consultDetails[0].chargeAmount.value"))
                .as("FEE_BATCH_CONSULT chargeAmount value should be 32049")
                .isEqualTo(32049);

        PGPHelpers.getTxnStatus(mid.getId(), orderId);
        JsonPath txnStatusJson = PGPHelpers.getTxnStatusResponse(mid.getId(), orderId).jsonPath();
        softAssert.assertThat(txnStatusJson.getString("chargeAmount"))
                .as("TxnStatus chargeAmount should be 320.49")
                .isEqualTo("320.49");

        softAssert.assertAll();
    }
}

