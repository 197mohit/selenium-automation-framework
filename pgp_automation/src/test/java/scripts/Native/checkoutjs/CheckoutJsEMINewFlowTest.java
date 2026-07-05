package scripts.Native.checkoutjs;


import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.LocalConfig;

import java.util.ArrayList;

import static com.paytm.appconstants.Constants.MerchantType.EMI_DISCOVERY;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.PGPHelpers.pause;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import io.restassured.path.json.JsonPath;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import io.restassured.path.json.JsonPath;

import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import io.restassured.response.Response;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;

public class CheckoutJsEMINewFlowTest extends PGPBaseTest {

    /* These Cases are for the New EMI Flow so,
    the MID used in these cases should be set to the EMI Branch of checkoutJs using Versioning before Running */

    private final CheckoutJsCheckoutPage checkoutPage =new CheckoutJsCheckoutPage();

    private void kfs(CashierPage cashierPage,PaymentDTO paymentDTO, Boolean isDC){
        try {
            cashierPage.viewAllOffersAvialable().click();
        } catch (AssertionError E) {
            //
        }
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        if (isDC) {
            cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
            cashierPage.checkEMIEligibility().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
        }
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(emidetails.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(emidetails.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(emidetails.getCvvNumber());
    }

    private void kfsSavedCard(CashierPage cashierPage,PaymentDTO paymentDTO, Boolean isDC){
        try {
            cashierPage.viewAllOffersAvialable().click();
        } catch (AssertionError E) {
            //
        }
        cashierPage.tabSavedEmi().click();
        pause(2);
        DriverManager.getDriver().switchTo().defaultContent();
        if (isDC) {
            cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
            cashierPage.checkEMIEligibility().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
        }
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        DriverManager.getDriver().switchTo().frame(cashierPage.Cvv_cardIframe());
        cashierPage.textBoxCVVNumberSavedcardEMI().clearAndType(emidetails.getCvvNumber());
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-54006")
    @Parameters({"theme"})
    @Test(description = "Test that Kfs link is shown when showKFSLink is true and showLendingConsent true for New Card Txn")
    public void Kfs_true_lendingConsent_true_newCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,"","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        kfs(cashierPage,paymentDTO,true);
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText_NewFlow().getText();
        Assertions.assertThat(adhere).contains("Key Fact Statement");
        Assertions.assertThat(adhere).contains("Digital Lending Consent");
        cashierPage.kfsLink().click();
        Thread.sleep(2000);
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        cashierPage.kfsUpperText().assertVisible();
        cashierPage.kfsLoanConsentText().assertVisible();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-54006")
    @Parameters({"theme"})
    @Test(description = "Test that Kfs link is shown when showKFSLink is true and showLendingConsent false for New Card Txn")
    public void Kfs_true_lendingConsent_false_newCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,"","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.DEBIT_CARD_NUMBER);
        kfs(cashierPage,paymentDTO,true);
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText_NewFlow().getText();
        Assertions.assertThat(adhere).contains("Key Fact Statement");
        cashierPage.kfsLink().click();
        Thread.sleep(2000);
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        cashierPage.kfsUpperText().assertVisible();
        cashierPage.kfsLoanConsentText().assertNotVisible();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-54006")
    @Parameters({"theme"})
    @Test(description = "Test that Kfs link is not shown when showKFSLink is false and showLendingConsent false for New Card Txn")
    public void Kfs_false_lendingConsent_false_newCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,"","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.EMI_CC);
        kfs(cashierPage,paymentDTO,false);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertNotVisible();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-54006")
    @Parameters({"theme"})
    @Test(description = "Test that Kfs link is shown when showKFSLink is true and showLendingConsent true for Saved Card Txn")
    public void Kfs_true_lendingConsent_true_savedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        String CustId = "EMITEST"+CommonHelpers.getRandomWithSize(6);
//        SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(merchantType, CustId,paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        String txnAmount="1100";
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfsSavedCard(cashierPage,paymentDTO,true);
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText_NewFlow().getText();
        Assertions.assertThat(adhere).contains("Key Fact Statement");
        Assertions.assertThat(adhere).contains("Digital Lending Consent");
        cashierPage.kfsLink().click();
        Thread.sleep(2000);
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        cashierPage.kfsUpperText().assertVisible();
        cashierPage.kfsLoanConsentText().assertVisible();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-54006")
    @Parameters({"theme"})
    @Test(description = "Test that Kfs link is shown when showKFSLink is true and showLendingConsent false for Saved Card Txn")
    public void Kfs_true_lendingConsent_false_savedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.DEBIT_CARD_NUMBER);
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        String CustId = "EMITEST"+CommonHelpers.getRandomWithSize(6);
//        SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(merchantType, CustId,paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        String txnAmount="1100";
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfsSavedCard(cashierPage,paymentDTO,true);
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText_NewFlow().getText();
        Assertions.assertThat(adhere).contains("Key Fact Statement");
        Assertions.assertThat(adhere).doesNotContain("Digital Lending Consent");
        cashierPage.kfsLink().click();
        Thread.sleep(2000);
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        cashierPage.kfsUpperText().assertVisible();
        cashierPage.kfsLoanConsentText().assertNotVisible();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-54006")
    @Parameters({"theme"})
    @Test(description = "Test that Kfs link is not shown when showKFSLink is false and showLendingConsent false for Saved Card Txn")
    public void Kfs_false_lendingConsent_false_SavedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.EMI_CC);
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_NEW_FLOW;
        String CustId = "EMITEST"+CommonHelpers.getRandomWithSize(6);
//        SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(merchantType, CustId,paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        String txnAmount="1100";
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        kfsSavedCard(cashierPage,paymentDTO,false);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertNotVisible();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(PUSPA)
    @Feature("PGP-55426")
    @Parameters({"theme"})
    @Test(description = "Verify APR text pop up box when mouse click on icon")
    public void verifyAprTextPopUp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true",txnAmount,"","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.DEBIT_CARD_NUMBER);

        cashierPage.viewAllOffersAvialable().click();
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
        cashierPage.checkEMIEligibility().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.clickAPRIcon().click();
        Assertions.assertThat(cashierPage.verifyAPRTittle().getText()).isEqualTo("Annual Percentage Rate (APR)");
        Assertions.assertThat(cashierPage.verifyAPRText().getText()).isEqualTo("Annual Percentage Rate (APR) is the effective annualised rate charged to the customer and is based on an all-inclusive cost which includes the interest and processing fee.");
        cashierPage.closeAPRButton().click();


    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56884")
    @Parameters({"theme"})
    @Test(description="Verify that the Applied Offer is shown on UI for  Saved Instruments for FPO changed response")
    public void testOfferAppliedOnSavedInstrument_ForFPOChangedResponse(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO= new PaymentDTO().setEmiCard(PaymentDTO.EMI_CC);
        Constants.MerchantType mid= Constants.MerchantType.EMI_NEW_FLOW;
        String CustId = "EMITEST"+CommonHelpers.getRandomWithSize(6);
//        SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        String transactionAmount="800";
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", transactionAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid)
                .setTxnValue(transactionAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (cashierPage.viewAllOffersAvialable().isElementPresent()){
            cashierPage.viewAllOffersAvialable().click();
        }
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.tabSavedCard().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-57680")
    @Parameters({"theme"})
    @Test(description = "Verify original card hash for EMI using visa credit card with offers on checkoutJS")
    public void verifyOriginalCardHashForEMIVisaCreditCardWithOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        String txnAmount="800";
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("\"originalCardHash\":\"2b2c999c78ecf405b7fb4efec1f1dd0c249a69477c906d8274cec376f247d2f0\"");
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-57680")
    @Parameters({"theme"})
    @Test(description = "Verify original card hash for Emi using visa credit card without offers on checkoutJS")
    public void verifyOriginalCardHashForEMIVisaCreditCardWithoutOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        String txnAmount="800";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("\"originalCardHash\":\"2b2c999c78ecf405b7fb4efec1f1dd0c249a69477c906d8274cec376f247d2f0\"");
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-57680")
    @Parameters({"theme"})
    @Test(description = "Verify original card hash for EMI using debit card with offers on checkoutJS")
    public void verifyOriginalCardHashForEMIDebitCardWithOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        String txnAmount="800";
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setCustId("MOCKGC0007")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage,paymentDTO,true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("\"originalCardHash\":\"84aa388be1d29fe348ec307822f9ea8a19f6c577ccc3a22160b0e6860abb2e94\"");
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-57680")
    @Parameters({"theme"})
    @Test(description = "Verify original card hash using master card  on checkoutJS\"")
    public void verifyOriginalCardHashFlowForMasterCard(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        String txnAmount="800";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                // .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("\"originalCardHash\":\"2b2c999c78ecf405b7fb4efec1f1dd0c249a69477c906d8274cec376f247d2f0\"");
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-57680")
    @Parameters({"theme"})
    @Test(description = "Verify original card hash using rupay card on checkoutJS\"")
    public void verifyOriginalCardHashFlowForRupayCard(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        String txnAmount="800";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.RUPAY_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("\"originalCardHash\":\"711c56c7640eec11e0f91af0fa58c2e199887527c60a3d21ca0dda064f51a21c\"");
    }

    //for this test case ff4j should be on for the used mid(theia.sendOriginalCardHash)
    @Owner(LOKESH_SAXENA)
    @Feature("PGP-57680")
    @Parameters({"theme"})
    @Test(description = "Verify original card hash using visa card on checkoutJS\"")
    public void verifyOriginalCardHashFlowForVisaCard(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        String txnAmount="800";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("\"originalCardHash\":\"2b2c999c78ecf405b7fb4efec1f1dd0c249a69477c906d8274cec376f247d2f0\"");
    }
    @Owner(RONIKA)
    @Feature("PGP-58208")
    @Parameters({"theme"})
    @Test(description = "Verify FetchBin response for EMI ineligible bin")
    public void verifyFetchBinResponseForEMIIneligibleBin(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        String txnAmount="800";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "429344").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.isEmiAvailable")).isEqualTo("false");
        Assertions.assertThat(fetchBinsJson.getString("body.errorMessage")).isEqualTo("EMI not available");
        Assertions.assertThat(fetchBinsJson.getString("body.emiChannel")).isNull();
        
    }
    @Owner(RONIKA)
    @Feature("PPSL-794")
    @Parameters({"theme"})
    @Test(description = "Verify FetchBin response for Bin which starts with zero")
    public void verifyFetchBinResponseForBinThatStartsWithZero(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =EMI_DISCOVERY;
        String txnAmount="800";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "047186").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid BIN format. BIN cannot start with Zero.");
        
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-59331")
    @Parameters({"theme"})
    @Test(description = "Saved card should not be visible on checkout page when theis preference HIDE_SAVED_CARDS_JS is on")
    public void EatSureHideSavedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        PaymentDTO paymentDTO= new PaymentDTO();
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_REG_HIDE_SAVED_CARD_MID;
        String CustId = "EMITEST"+CommonHelpers.getRandomWithSize(6);
        SavedCardHelpers.addCardOnMidCustId(merchantType, CustId,paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        String txnAmount="1100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.tabSavedCard().assertNotVisible();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(merchantType.getId())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setDeepLinkRequiedField(true)
                .setRequestId(String.valueOf(System.currentTimeMillis()))
                .setWorkFlow("checkout")
                .setVersion("v3")
                .build();
        
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchantType.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", "checkout0201");
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("orderId", initTxnDTO.orderFromBody());
        fetchPaymentOption.getRequestSpecBuilder().addHeader("accept", "*/*");
        fetchPaymentOption.getRequestSpecBuilder().addHeader("accept-language", "en-GB,en-US;q=0.9,en;q=0.8");
        fetchPaymentOption.getRequestSpecBuilder().addHeader("priority", "u=1, i");
        fetchPaymentOption.getRequestSpecBuilder().addHeader("referer", "https://pgp-ite.paytm.in/checkoutjs/1448/assets/iframes/dummy-frame.html");
        fetchPaymentOption.getRequestSpecBuilder().addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
        Response response = fetchPaymentOption.execute();
        JsonPath fetchPaymentOptionsJson = response.jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        int savedInstrumentsSize = fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").size();
        Assertions.assertThat(savedInstrumentsSize).isEqualTo(0);
    } 

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-59294")
    @Parameters({"theme"})
    @Test(description = "Only restricted tenures paased in initiate request will lead to success txn for correct tenures in process txn")
    public void RestrictedTenuresPassedInInitiateRequestWillLeadToSuccessTxnForCorrectTenuresInProcessTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        SoftAssertions softly = new SoftAssertions();
        Constants.MerchantType merchant = EMI_DISCOVERY;
        String txnAmount = "800";
        ArrayList<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("15031116688", "123", "18084", 400.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("15031116689", "123", "18084", 400.00, "6224");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(
                new ArrayList<>(), "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new SimplifiedUnifiedOffers.SubventionDetails(
                "false", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails, subventionDetails, items);
        EnablePaymentMode emiMode = new EnablePaymentMode();
        emiMode.setMode("EMI");
        emiMode.setBanks(new String[]{"HDFC", "ICICI"});
        emiMode.setAdditionalProperty("tenures", new String[]{"3", "6"});
        
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setCallbackUrl(LocalConfig.MOCK_HOST + "/mockbank" + Constants.PagePath.COMMON_RESPONSE_PAGE_PATH)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setEnablePaymentMode(new EnablePaymentMode[]{emiMode})
                .setCustId("1000036031")
                .setWebsiteName("retail")
                .build();
        
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String cardBin = paymentDTO.getEmiCard().substring(0, 6);
        FetchBinDetailsRequest fetchBinRequest = new FetchBinDetailsRequest.Builder(txnToken, cardBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinRequest, merchant.getId(), initTxnDTO.orderFromBody());
        JsonPath fetchBinResponse = fetchBinDetail.execute().jsonPath();
        softly.assertThat(fetchBinResponse.getString("body.isEmiAvailable")).isEqualTo("true");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v3")
                .setWorkFlow("checkout")
                .setFetchAllPaymentOffers("true")
                .setApplyPaymentOffers("true")
                .setDeepLinkRequiedField(true)
                .setRequestId(String.valueOf(System.currentTimeMillis()))
                .build();
        
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder()
                .addQueryParam("referenceId", "checkout0201")
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
                .addHeader("priority", "u=1, i")
                .addHeader("referer", Constants.CheckoutJsReferer.CHECKOUTJS_REFERER)
                .addHeader("sec-ch-ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Google Chrome\";v=\"138\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
        
        JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
        String tenures = fpoResponse.getString("body.tenuresForOrOffers.find { it.mode == 'EMI' && it.banks.contains('HDFC') }.tenures");
        softly.assertThat(tenures).contains("3");
        softly.assertThat(tenures).contains("6");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        softly.assertAll();
    }
}