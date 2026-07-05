package scripts.EMITest.CheckoutJS;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.LOKESH_SAXENA;
import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static com.paytm.dto.PaymentDTO.*;

public class EMICheckoutJsNewFlowTest extends PGPBaseTest {

    private static final String RUPAY_CC_CARD_NO = AlternateID_RUPAY_CARD;
    private static final String VISA_CC_CARD_NO = AlternateID_VISA_CARD;
    private static final String MASTER_ICICI_DC_NUMBER = MASTER_ICICI_DEBIT_CARD_NUMBER;
    private static final String DINERS_CC_CARD_NO = DINERS_CC_CARD_NUMBER;

    private final CheckoutJsCheckoutPage checkoutPage =new CheckoutJsCheckoutPage();
    private final CheckoutJsCheckoutMerchantElementPage elementPage =new CheckoutJsCheckoutMerchantElementPage();
    private final CheckoutPage checkoutRedirectionPage = new CheckoutPage();

    //Function to open Checkout Js/ElementJs/Js on redirection UI
    //For ElementJs, payMode is mandatory and must be one of - CARD,EMI,NB,UPI
    private String openJs(String JsType, InitTxnDTO initTxnDTO, String txnToken, Constants.MerchantType mid,String payMode) throws IOException {
        if (JsType.toLowerCase().equals("checkoutjs")) {
            MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, "checkoutjs_web_revamp");
            config.data.setToken(txnToken);
            checkoutPage.createCheckoutJsOrder(config);
            return "checkoutjs_web_revamp";
        } else if(JsType.toLowerCase().equals("elementjs")){
            MerchantConfig config = elementPage.loadMerchantConfig(initTxnDTO, "checkoutjse_web_revamp");
            config.data.setToken(txnToken);
            elementPage.createCheckoutJsOrder(config);
            elementPage.createAndInvokePaymode(payMode);
            return "checkoutjse_web_revamp";
        }else if(JsType.toLowerCase().equals("jsonredirection")){
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mid, initTxnDTO.orderFromBody(), txnToken).build();
            checkoutRedirectionPage.createAppInvokeOrder(orderDTO);
            return "checkoutjs_web_revamp";
        }
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, JsType);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        return "checkoutjs_web_revamp";
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with RUPAY CARD , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_RUPAY_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(RUPAY_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with RUPAY CARD , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_RUPAY_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(RUPAY_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with VISA CARD , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_VISA_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with VISA CARD , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_VISA_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with MASTER CARD , new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_BO_MASTER_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(MASTER_ICICI_DC_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assert.assertNotNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with MASTER CARD , new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_BO_MASTER_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_MASTER;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(MASTER_ICICI_DC_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assert.assertNotNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with DINERS CARD , new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_BO_DINERS_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_DINERS;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(DINERS_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assert.assertNotNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with DINERS CARD , new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_BO_DINERS_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_DINERS;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(DINERS_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assert.assertNotNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }



   @Owner(MEHUL_GUPTA)
   @Parameters({"JsType"})
   @Test(description = "JS Checkout with RUPAY Saved CARD , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
   public void testJsCheckoutNewFlow_EMI_BO_RUPAY_SAVED_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
       User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
       String txnAmount = "1100";
       Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
       PaymentDTO paymentDTO = new PaymentDTO();
       paymentDTO.setEmiCard(RUPAY_CC_CARD_NO);
       String CustId = "EMITEST"+CommonHelpers.generateOrderId();
       //SavedCardHelpers.deleteSavedCard(CustId);
      SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
       ArrayList<String> promoCode = new ArrayList<>();
       promoCode.add("");
       List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
       String orderId = CommonHelpers.generateOrderId();
       SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
       items.add(item1);
       SimplifiedUnifiedOffers.PromoDetails promoDetails = new
               SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
       SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
               SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
       SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
               SimplifiedUnifiedOffers(subventionDetails, promoDetails);
       simplifiedUnifiedOffers.setItem(items);
       InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid)
               .setTxnValue(txnAmount)
               .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
               .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
               .setCustId(CustId)
               .build();
       String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
       String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
       CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       cashierPage.payByEMISavedCard(cashierPage, paymentDTO,false);
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
               .validateTxnDate(new Date())
               .validateTxnId(Constants.ValidationType.NON_EMPTY)
               .assertAll();
       TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
       txnStatus.executeUntilNotPending();
       Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
       Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
       Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
       Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
   }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with RUPAY Saved CARD , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_RUPAY_SAVED_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(RUPAY_CC_CARD_NO);
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByEMISavedCard(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with VISA Saved CARD , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_VISA_SAVED_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByEMISavedCard(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with VISA Saved CARD , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutNewFlow_EMI_BO_VISA_SAVED_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setCustId(CustId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByEMISavedCard(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Feature("PGP-58730")
    @Test(description = "Js Checkout with CC with only offer , new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_CC_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);

        //PGP-58730 Test originalCardHash being sent in extendInfo and channelInfo in COP/CO&P and hash is created using Card Number
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}");  // Fix object boundaries
        JsonPath jsonPath = new JsonPath(cleanedJson);
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("originalCardHash:63faa35b125e9ba82d2e13fc2be21955434ce1f2f44150d0809221fcafb5423b");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:63faa35b125e9ba82d2e13fc2be21955434ce1f2f44150d0809221fcafb5423b");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with Saved Card with only offer , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_SavedCard_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getCreditCardNumber());
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBySavedCard(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with NB with only offer , new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_NB_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.payByNB(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with UPI with only offer , new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_UPI_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        //paymentDTO.setVpa("paytm.uat@axis");
        cashierPage.payByUPI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads("150");
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI with only offer , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI Saved Card with only offer , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMISavedCard_with_Offer(@Optional("checkoutJs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payByEMISavedCard(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Feature("PGP-58730")
    @Test(description = "Js Checkout with EMI_DC with only offer , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_DC_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(MASTER_ICICI_DC_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,true);

        //PGP-58730 Test originalCardHash being sent in extendInfo and channelInfo in COP/CO&P and hash is created using Card Number
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}");  // Fix object boundaries
        JsonPath jsonPath = new JsonPath(cleanedJson);
        Assertions.assertThat(jsonPath.getString("REQUEST.extendInfo")).contains("originalCardHash:ae6dd66df28c6e5a543d9e7efa752ca053914a9dcd56c7c3e1eada8971c61dfc");
        Assertions.assertThat(jsonPath.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:ae6dd66df28c6e5a543d9e7efa752ca053914a9dcd56c7c3e1eada8971c61dfc");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI Bank Flow with only offer , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_Bank_Flow_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMIBankFlow(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Multiple Items , new flow - BO and Subvention Applied")
    public void testJsCheckoutNewFlow_MultiItem_Subvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Single Items , new flow - BO and Subvention Applied")
    public void testJsCheckoutNewFlow_SingleItem_Subvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Multiple Item , new flow - Only BO CC Txn")
    public void testJsCheckoutNewFlow_MultiItem_NoSubvention_And_BO_CCTxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Single Item , new flow - Only BO CC Txn")
    public void testJsCheckoutNewFlow_SingleItem_NoSubvention_And_BO_CCTxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Multiple Item , new flow - Only Subvention Applied ")
    public void testJsCheckoutNewFlow_MultiItem_Subvention_And_NoBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6223");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123046", "18084", 1100.00, "16225");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Single Item , new flow - Only Subvention Applied ")
    public void testJsCheckoutNewFlow_SingleItem_Subvention_And_NoBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6223");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Multiple Items , new flow - Only BO Standard EMI Txn")
    public void testJsCheckoutNewFlow_MultiItem_NoSubvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123046", "18084", 1100.00, "6225");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Single Item , new flow - Only Subvention Applied ")
    public void testJsCheckoutNewFlow_SingleItem_NoSubvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6224");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Amount based BO and multiple item based subvention , new flow ")
    public void testJsCheckoutNewFlow_MultiItem_ItemBasedSubvention_And_AmountBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 400.00, "186414");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47178", "92226", 400.00, "186416");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for Amount based BO and single item based subvention , new flow ")
    public void testJsCheckoutNewFlow_SingleItem_ItemBasedSubvention_And_AmountBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 400.00, "186414");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for multiple item based BO and Amount based subvention , new flow ")
    public void testJsCheckoutNewFlow_MultiItem_AmountBasedSubvention_And_ItemBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "1707", 400.00, "86414");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47176", "1707", 400.00, "86414");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for single item based BO and Amount based subvention , new flow")
    public void testJsCheckoutNewFlow_SingleItem_AmountBasedSubvention_And_ItemBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "1707", 400.00, "86414");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for amount based BO and amount based subvention , new flow ")
    public void testJsCheckoutNewFlow_AmountBasedSubvention_And_AmountBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for 6 digit Bin offers, new flow - CC Txn")
    public void testJsCheckoutNewFlow_6digitBin(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_6DIGIT_BIN;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout for 8 digit Bin offers, new flow - CC Txn")
    public void testJsCheckoutNewFlow_8digitBin(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_8DIGIT_BIN;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with CC with offer and PCF , new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_CC_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isGreaterThan(new Float(0.00));
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with NB with offer and PCF , new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_NB_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.payByNB(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isGreaterThan(new Float(0.00));
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

//    @Owner(MEHUL_GUPTA)
//    @Parameters({"JsType"})
//    @Test(description = "Js Checkout with UPI with offer and PCF , new flow - Bank offers Applied, Amount Based")
//    public void testJsCheckoutNewFlow_UPI_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
//        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
//        String txnAmount = "800";
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
//        ArrayList<String> promoCode = new ArrayList<>();
//        promoCode.add("");
//        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
//                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
//        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
//                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
//        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
//                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
//        InitTxnDTO initTxnDTO = new InitTxnDTO.
//                Builder(user.ssoToken(), mid)
//                .setTxnValue(txnAmount)
//                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
//                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
//                .build();
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
//        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"UPI");
//        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
//        PaymentDTO paymentDTO = new PaymentDTO();
//        //paymentDTO.setVpa("paytm.uat@axis");
//        cashierPage.payBy(Constants.PayMode.UPI, paymentDTO);
//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads("150");
//        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
//                .validateCurrency("INR")
//                .validateMid(initTxnDTO.getBody().getMid())
//                .validateOrderId(initTxnDTO.getBody().getOrderId())
//                .validatePaymentMode("UPI")
//                .validateRespCode("01")
//                .validateRespMsg("Txn Success")
//                .validateStatus("TXN_SUCCESS")
//                .validateTxnDate(new Date())
//                .validateTxnId(Constants.ValidationType.NON_EMPTY)
//                .assertAll();
//        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
//        txnStatus.executeUntilNotPending();
//        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isEqualTo(new Float(0.0));
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
//    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI with offer and PCF , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isGreaterThan(new Float(0.00));
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI_DC with offer and PCF , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_DC_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        String custId = "MOCKGC10001";
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, mid)
                .setTxnValue(txnAmount)
                .setCustId(custId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(MASTER_ICICI_DC_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isGreaterThan(new Float(0.00));
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI Bank Flow with offer and PCF , new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutNewFlow_EMI_Bank_Flow_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMIBankFlow(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isGreaterThan(new Float(0.00));
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI for Merchant with preference MINIMAL_PROMO_MERCHANT")
    public void testJsCheckoutNewFlow_EMI_MINIMAL_PROMO_MERCHANT(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_PROMO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with CC for Merchant with preference MINIMAL_PROMO_MERCHANT")
    public void testJsCheckoutNewFlow_CC_MINIMAL_PROMO_MERCHANT(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_PROMO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI for Merchant with preference MINIMAL_SUBVENTION_MERCHANT")
    public void testJsCheckoutNewFlow_EMI_MINIMAL_SUBVENTION_MERCHANT(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_SUBVENTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with CC for Merchant with preference MINIMAL_SUBVENTION_MERCHANT")
    public void testJsCheckoutNewFlow_CC_MINIMAL_SUBVENTION_MERCHANT(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_SUBVENTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI for Merchant with preference MINIMAL_SUBVENTION_MERCHANT and MINIMAL_PROMO_MERCHANT")
    public void testJsCheckoutNewFlow_EMI_MINIMAL_SUBVENTION_PROMO_MERCHANT(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_PROMO_SUBVENTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with CC for Merchant with preference MINIMAL_SUBVENTION_MERCHANT and MINIMAL_PROMO_MERCHANT")
    public void testJsCheckoutNewFlow_CC_MINIMAL_SUBVENTION_PROMO_MERCHANT(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_PROMO_SUBVENTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"SAVED_CARD");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(VISA_CC_CARD_NO);
        cashierPage.payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }
    
    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Feature("PGP-58212")
    @Test(description = "Verify that for CC paymode offerApply is not called when unifiedOffers does not have CARD_LINKED offers")
    public void testJsCheckoutNewFlow_UnifiedOffers_Empty_CC(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "370";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_RESTRICT_OFFER;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payByCC(cashierPage, paymentDTO);
        cashierPage.pause(2);

        String reqresp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(reqresp).contains("/theia/api/v1/initiateTransaction").contains("/theia/api/v1/processTransaction");
        Assertions.assertThat(reqresp).doesNotContain("/theia/api/v1/offerApply");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Feature("PGP-58212")
    @Test(description = "Verify that for EMI paymode offerApply is called when unifiedOffers does not have EMI_LINKED offers")
    public void testJsCheckoutNewFlow_UnifiedOffers_Empty_EMI(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "770";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_RESTRICT_OFFER;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
        cashierPage.pause(2);

        String reqresp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(reqresp).contains("/theia/api/v1/initiateTransaction").contains("/theia/api/v1/processTransaction");
        Assertions.assertThat(reqresp).contains("/theia/api/v1/offerApply");

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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }
    
    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with EMI Credit Card, EmiOfferDetailList should be visible for  Amount Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_AmountBased_EmiCC(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "500";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(), "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "500", "2434288", null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCustId(user.custId())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");

    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with Bank Flow, EmiOfferDetailList should be visible for  Amount Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_AmountBased_Bankflow(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "500";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(), "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "500", "2434288", null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCustId(user.custId())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMIBankFlow(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");

    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with Saved Card, EmiOfferDetailList should be visible for  Amount Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_AmountBased_SavedCard(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "500";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(), "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "500", "2434288", null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCustId("EMITEST9694f01139e843a789f17972f7b916fe")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBySavedCard(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");

    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with EMI Credit Card, EmiOfferDetailList should be visible for  Item Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_SingleItemBased_EMICC(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.EMIDCELIGIBLE);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436649"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436650"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2432061"));
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items("15031116688","123047","18084",1100.00,"6226","2432015",bankOfferDetailsList);
        items.add(item);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(),"true","false","false",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new SimplifiedUnifiedOffers.SubventionDetails("false",null,null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails,subventionDetails,items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
            .setTxnValue("1100")
            .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
            .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
            .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String processTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP, initTxnDTO.orderFromBody(),"/theia/api/v1/processTransaction","REQUEST");
        String pgpId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("PGP_ID", processTxnLogs).replace("\"", "");
        System.out.println("Extracted PGP_ID: " + pgpId); 
        Assertions.assertThat(pgpId).isNotNull().as("PGP_ID should not be null");
        String offerApplyLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.theia_facade,pgpId,"v2/offer/apply","REQUEST");
        Assertions.assertThat(offerApplyLogs).contains("\"emiOfferDetailsList\"");
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");
    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with Bank Flow, EmiOfferDetailList should be visible for  Item Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_SingleItemBased_BankFlow(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.EMIDCELIGIBLE);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436649"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436650"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2432061"));
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items("15031116688","123047","18084",1100.00,"6226","2432015",bankOfferDetailsList);
        items.add(item);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(),"true","false","false",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new SimplifiedUnifiedOffers.SubventionDetails("false",null,null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails,subventionDetails,items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
            .setTxnValue("1100")
            .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
            .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
            .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMIBankFlow(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String processTxnLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,initTxnDTO.orderFromBody(),"/theia/api/v1/processTransaction","REQUEST");
        String pgpId = PG2LogsValidationHelper.getKeyParameterValueFromLogs("PGP_ID", processTxnLogs).replace("\"", "");
        System.out.println("Extracted PGP_ID: " + pgpId); 
        Assertions.assertThat(pgpId).isNotNull().as("PGP_ID should not be null");
        String offerApplyLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.theia_facade,pgpId,"v2/offer/apply","REQUEST");
        Assertions.assertThat(offerApplyLogs).contains("\"emiOfferDetailsList\"");
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");
    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with EMI Credit Card, EmiOfferDetailList should be visible for  Item Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_MultipleItemBased_EMICC(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436649"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436650"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2432061"));
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items("15031116688","123047","18084",5000.00,"6226","2432015",bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("15031116689","123047","18084",5000.00,"6226","2432015",bankOfferDetailsList);
        items.add(item);
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(),"true","false","false",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new SimplifiedUnifiedOffers.SubventionDetails("false",null,null,null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails,subventionDetails,items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
            .setTxnValue("10000")
            .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
            .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
            .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");
    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for new flow with Saved Card, EmiOfferDetailList should be visible for Multiple Item Based")
    public void testJsCheckoutNewFlow_ForEmiOfferDetailList_MultipleItemBased_SavedCard(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436649"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2436650"));
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2432061"));
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items("15031116688","123047","18084", 5000.00,      "6226",  "2432015",bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("15031116689","123047", "18084",5000.00, "6226",   "2432015",bankOfferDetailsList);
        items.add(item);
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(),  "true","false","false",null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new SimplifiedUnifiedOffers.SubventionDetails("false",null,null,null );
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails,subventionDetails,items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
            .setTxnValue("10000")
            .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
            .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
            .setCustId("EMITEST9694f01139e843a789f17972f7b916fe")
            .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBySavedCard(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery","REQUEST");
        Assertions.assertThat(discoveryLogs).contains("\"emiOfferDetailsList\"");
        String dicoveryLiteLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","REQUEST");
        Assertions.assertThat(dicoveryLiteLogs).contains("\"emiOfferDetailsList\"");
    }


    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "Verify UPI Offers getting applied in UPI Collect flow")
    public void VerifyUPIOffersGettingAppliedInUPICollectFlowItemBased(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String custID = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items("15031116688","123047","18084", 800.00,      "6226");
        items.add(item);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(),  "true","false","false",null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails,items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
            .setTxnValue("800")
            .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
            .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
            .setCustId(custID)
            .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.offerStripVisible().waitUntilVisible();
        cashierPage.offerStripVisible().assertVisible();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payByUPI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
        String offerapply = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/offer/apply", "RESPONSE");
        Assertions.assertThat(offerapply).contains("discount applied successfully.");
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","RESPONSE");
        Assertions.assertThat(discoveryLogs).contains("\"issuingBank\":\"UPI\"");
    }


    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "Verify UPI Offers getting applied in UPI Collect flow")
    public void VerifyUPIOffersGettingAppliedInUPICollectFlowAmountBased(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        String custID = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(new ArrayList<>(),  "true","false","true",null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
            .setTxnValue("800")
            .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
            .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
            .setCustId(custID)
            .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.offerStripVisible().waitUntilVisible();
        cashierPage.offerStripVisible().assertVisible();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payByUPI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
        String offerapply = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/offer/apply", "RESPONSE");
        Assertions.assertThat(offerapply).contains("discount applied successfully.");
        String discoveryLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,mid.getId(),"v2/offer/discovery/lite","RESPONSE");
        Assertions.assertThat(discoveryLogs).contains("\"issuingBank\":\"UPI\"");
    }

}
