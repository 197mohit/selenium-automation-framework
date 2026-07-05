package scripts.SimplifiedUnifiedOffers;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckoutJsSimplifiedUnifiedOffer extends CheckoutJsBase {

    private void clearNetworkLogs() {
        DriverManager.getDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
    }

    private void ValidateNetworkLogOfferApply(String val, boolean exist) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<LogEntry> logList = DriverManager.getDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
        boolean isPresent = false;
        for (LogEntry entry : logList) {
            String mess = entry.getMessage();
            if (mess.contains("Network.requestWillBeSent") && mess.contains("offerApply") && mess.contains(val)) {
                isPresent = true;
                break;
            }
        }
        Assertions.assertThat(exist).withFailMessage("check log for exist: " + exist + ", text present in log is : " + isPresent).isEqualTo(isPresent);
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of CC with Discounted Promo provided in initTxn api")
    public void CCSuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
   //     clearNetworkLogs();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
//        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyOfferText().assertVisible();
        Thread.sleep(2000);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of DC with Discounted Promo provided in initTxn api")
    public void DCSuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
        //     clearNetworkLogs();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
//        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyOfferText().assertVisible();
        Thread.sleep(2000);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of NB with Discounted Promo provided in initTxn api")
    public void NBSuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setBankName("ICICI");
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        //     clearNetworkLogs();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        cashierPage.applyOfferText().assertVisible();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of UPI with Discounted Promo provided in initTxn api with upi VPA")
    public void UPIVPASuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.payBy(Constants.PayMode.UPI);
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of UPI with Discounted Promo provided in initTxn api with mobile number")
    public void UPINumricSuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of Saved Card with Discounted Promo provided in initTxn api")
    public void SavedCardSuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabSavedCard());
        //      cashierPage.OfferStripSavedPaymode().assertVisible();
        cashierPage.tabSavedCard().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.textBoxSavedCardCVV().clearAndType(paymentDTO.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of Saved VPA with Discounted Promo provided in initTxn api")
    public void SavedVpaSuccessTxnDiscountedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        String txnAmount="1100";
    //    SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabUPISavedVPA());
        cashierPage.OfferStripSavedPaymode().assertVisible();
        Thread.sleep(1000);
        cashierPage.tabUPISavedVPA().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.proceedButton().waitUntilClickable();
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of CC with Cashback Promo provided in initTxn api")
    public void CCSuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
        //     clearNetworkLogs();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
//        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyOfferText().assertVisible();
        Thread.sleep(2000);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of DC with Cashback Promo provided in initTxn api")
    public void DCSuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
        //     clearNetworkLogs();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
//        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyOfferText().assertVisible();
        Thread.sleep(2000);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of NB with Cashback Promo provided in initTxn api")
    public void NBSuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setBankName("ICICI");
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        //     clearNetworkLogs();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        cashierPage.applyOfferText().assertVisible();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of UPI with Cashback Promo provided in initTxn api with upi VPA")
    public void UPIVPASuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.payBy(Constants.PayMode.UPI);
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of UPI with Cashback Promo provided in initTxn api with mobile number")
    public void UPINumricSuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of Saved Card with Cashback Promo provided in initTxn api")
    public void SavedCardSuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabSavedCard());
        //      cashierPage.OfferStripSavedPaymode().assertVisible();
        cashierPage.tabSavedCard().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.textBoxSavedCardCVV().clearAndType(paymentDTO.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of Saved VPA with Cashback Promo provided in initTxn api")
    public void SavedVpaSuccessTxnCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        String txnAmount="1100";
        //    SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","2217116");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabUPISavedVPA());
        cashierPage.OfferStripSavedPaymode().assertVisible();
        cashierPage.tabUPISavedVPA().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.proceedButton().waitUntilClickable();
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of CC with best Promo applied")
    public void CCSuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
        //     clearNetworkLogs();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
//        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyOfferText().assertVisible();
        Thread.sleep(2000);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of DC with best Promo applied")
    public void DCSuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
        //     clearNetworkLogs();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
//        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyOfferText().assertVisible();
        Thread.sleep(2000);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of NB with Best Promo applied")
    public void NBSuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO= new PaymentDTO().setBankName("ICICI");
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        //     clearNetworkLogs();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        cashierPage.applyOfferText().assertVisible();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of UPI with best Promo applied")
    public void UPIVPASuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.payBy(Constants.PayMode.UPI);
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of UPI with Best Promo applied")
    public void UPINumricSuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        //        ValidateNetworkLogOfferApply("PROMO000123",true);
//        ValidateNetworkLogOfferApply("CREDIT_CARD",true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Test the success txn of Saved Card with Best Promo applied")
    public void SavedCardSuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        String txnAmount="1100";
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabSavedCard());
  //      cashierPage.OfferStripSavedPaymode().assertVisible();
        cashierPage.tabSavedCard().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.textBoxSavedCardCVV().clearAndType(paymentDTO.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Test the success txn of Saved VPA with Best Promo applied")
    public void SavedVpaSuccessTxnBestPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        String txnAmount="1100";
        //    SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType= Constants.MerchantType.SIMPLIFIED_OFFERS;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
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
        if(!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.scrollToElement(cashierPage.tabUPISavedVPA());
        cashierPage.OfferStripSavedPaymode().assertVisible();
        cashierPage.tabUPISavedVPA().click();
        cashierPage.applyOfferTextSavedInstruments().assertVisible();
        cashierPage.proceedButton().waitUntilClickable();
        cashierPage.proceedButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    // Need to run on branch which has simplified unified offers
    @Parameters({"theme"})
    @Test(description = "Test offer strip is visible for subvention")
    public void testOfferStripVisible(@Optional("checkoutjs_web_revamp") String theme) throws Exception {


        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "1100";
        Constants.MerchantType merchantType = Constants.MerchantType.SIMPLIFIED_OFFERS;
        SimplifiedUnifiedOffers.SubventionDetails subDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true","1100","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subDetails);
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
        if (!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.viewAllOffersAvialable().assertVisible();

    }


}
