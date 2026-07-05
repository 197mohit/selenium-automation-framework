package scripts.EMITest.CheckoutJS;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.NativeDTO.InitTxn.UserInfo;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.LOKESH_SAXENA;
import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static com.paytm.dto.PaymentDTO.*;

public class EMICheckoutJsOldFlowTest extends PGPBaseTest {

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
    @Test(description = "JS Checkout with RUPAY CARD , simplified Object migrated to new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_RUPAY_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with RUPAY CARD, simplified Object migrated to new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_RUPAY_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with VISA CARD , simplified Object migrated to new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_VISA_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with VISA CARD, simplified Object migrated to new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_VISA_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with MASTER CARD , simplified Object migrated to new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_MASTER_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with MASTER CARD, simplified Object migrated to new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_MASTER_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_MASTER;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Feature("PGP-58730")
    @Test(description = "JS Checkout with DINERS CARD , simplified Object migrated to new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_DINERS_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_DINERS;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(DINERS_CC_CARD_NO);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);

        //PGP-58730 Test originalCardHash being sent in extendInfo and channelInfo in COP/CO&P and hash is created using Card Number
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}");  // Fix object boundaries
        JsonPath acqLogs = new JsonPath(cleanedJson);
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo")).contains("originalCardHash:2e62a723688e023533db88817c634275ef42a6a1352dcd585dee05a584949811");
        Assertions.assertThat(acqLogs.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:2e62a723688e023533db88817c634275ef42a6a1352dcd585dee05a584949811");

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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with DINERS CARD, simplified Object migrated to new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_BO_DINERS_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_DINERS;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("planId");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout with RUPAY Saved CARD , simplified Object migrated to new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutOldFlow_EMI_BO_RUPAY_SAVED_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String orderId = CommonHelpers.generateOrderId();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(RUPAY_CC_CARD_NO);
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "JS Checkout with RUPAY Saved CARD , simplified Object migrated to new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutOldFlow_EMI_BO_RUPAY_SAVED_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(RUPAY_CC_CARD_NO);
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "JS Checkout with VISA Saved CARD , simplified Object migrated to new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutOldFlow_EMI_BO_VISA_SAVED_CARD_PAR_DISABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        String orderId = CommonHelpers.generateOrderId();

        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "JS Checkout with VISA Saved CARD , simplified Object migrated to new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testJsCheckoutOldFlow_EMI_BO_VISA_SAVED_CARD_PAR_ENABLE(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout with CC with only offer , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_CC_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout with Saved Card with only offer , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_SavedCard_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getCreditCardNumber());
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout with NB with only offer , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_NB_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout with UPI with only offer , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_UPI_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
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
    @Test(description = "Js Checkout with EMI with only offer , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout with EMI Saved Card with only offer , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMISavedCard_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        //SavedCardHelpers.deleteSavedCard(CustId);
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout with EMI_DC with only offer , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_DC_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI Bank Flow with only offer , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_Bank_Flow_with_Offer(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout for Multiple Items , simplified Object migrated to new flow - BO and Subvention Applied")
    public void testJsCheckoutOldFlow_MultiItem_Subvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;

        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        SimplifiedPaymentOffers.Items item2= new SimplifiedPaymentOffers.Items("Item002_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        itemsList.add(item2);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        SimplifiedSubvention.Item itemSub2 = new SimplifiedSubvention.Item("Item002_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        items.add(itemSub1);
        items.add(itemSub2);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout for Single Items , simplified Object migrated to new flow - BO and Subvention Applied")
    public void testJsCheckoutOldFlow_SingleItem_Subvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123047","18084", Arrays.asList("6226"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout for Multiple Item , simplified Object migrated to new flow - Only BO CC Txn")
    public void testJsCheckoutOldFlow_MultiItem_NoSubvention_And_BO_CCTxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        SimplifiedPaymentOffers.Items item2= new SimplifiedPaymentOffers.Items("Item002_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        itemsList.add(item2);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout for Single Item , simplified Object migrated to new flow - Only BO CC Txn")
    public void testJsCheckoutOldFlow_SingleItem_NoSubvention_And_BO_CCTxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6226");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123047", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout for Multiple Item , simplified Object migrated to new flow - Only Subvention Applied ")
    public void testJsCheckoutOldFlow_MultiItem_Subvention_And_NoBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123045","18084", Arrays.asList("6223"),"1","1100.0");
        SimplifiedSubvention.Item itemSub2 = new SimplifiedSubvention.Item("Item002_" + orderId,"123046","18084", Arrays.asList("16225"),"1","1100.0");
        items.add(itemSub1);
        items.add(itemSub2);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout for Single Item , simplified Object migrated to new flow - Only Subvention Applied ")
    public void testJsCheckoutOldFlow_SingleItem_Subvention_And_NoBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        String orderId = CommonHelpers.generateOrderId();

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123045","18084", Arrays.asList("6223"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout for Multiple Items , simplified Object migrated to new flow - Only BO Standard EMI Txn")
    public void testJsCheckoutOldFlow_MultiItem_NoSubvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "2200";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;

        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds1= new ArrayList<>();
        categoryIds1.add("6224");
        List<String> categoryIds2= new ArrayList<>();
        categoryIds2.add("6225");
        SimplifiedPaymentOffers.ProductDetail productDetail1= new SimplifiedPaymentOffers.ProductDetail("123045", null, "18084",categoryIds1);
        SimplifiedPaymentOffers.ProductDetail productDetail2= new SimplifiedPaymentOffers.ProductDetail("123046", null, "18084",categoryIds2);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail1);
        SimplifiedPaymentOffers.Items item2= new SimplifiedPaymentOffers.Items("Item002_" + orderId,null,"1100",productDetail2);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        itemsList.add(item2);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123045","18084", Arrays.asList("6224"),"1","1100.0");
        SimplifiedSubvention.Item itemSub2 = new SimplifiedSubvention.Item("Item002_" + orderId,"123046","18084", Arrays.asList("6225"),"1","1100.0");
        items.add(itemSub1);
        items.add(itemSub2);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout for Single Item , simplified Object migrated to new flow - Only BO Standard EMI Txn")
    public void testJsCheckoutOldFlow_SingleItem_NoSubvention_And_BO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;

        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds1= new ArrayList<>();
        categoryIds1.add("6224");
        SimplifiedPaymentOffers.ProductDetail productDetail1= new SimplifiedPaymentOffers.ProductDetail("123045", null, "18084",categoryIds1);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"1100",productDetail1);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"123045","18084", Arrays.asList("6224"),"1","1100.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    public void testJsCheckoutOldFlow_MultiItem_ItemBasedSubvention_And_AmountBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;

        String orderId = CommonHelpers.generateOrderId();

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"47176","92226", Arrays.asList("186414"),"1","400.0");
        SimplifiedSubvention.Item itemSub2 = new SimplifiedSubvention.Item("Item002_" + orderId,"47178","92226", Arrays.asList("186416"),"1","400.0");
        items.add(itemSub1);
        items.add(itemSub2);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    public void testJsCheckoutOldFlow_SingleItem_ItemBasedSubvention_And_AmountBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        String orderId = CommonHelpers.generateOrderId();

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");

        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("Item001_" + orderId,"47176","92226", Arrays.asList("186414"),"1","400.0");
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    public void testJsCheckoutOldFlow_MultiItem_AmountBasedSubvention_And_ItemBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;

        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("86414");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("47176", null, "1707",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"400",productDetail);
        SimplifiedPaymentOffers.Items item2= new SimplifiedPaymentOffers.Items("Item002_" + orderId,null,"400",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        itemsList.add(item2);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    public void testJsCheckoutOldFlow_SingleItem_AmountBasedSubvention_And_ItemBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;

        String orderId = CommonHelpers.generateOrderId();

        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("86414");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("47176", null, "1707",categoryIds);
        SimplifiedPaymentOffers.Items item1= new SimplifiedPaymentOffers.Items("Item001_" + orderId,null,"400",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(item1);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);

        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    public void testJsCheckoutOldFlow_AmountBasedSubvention_And_AmountBasedBO_EMITxn(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;

        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout for 6 digit Bin offers, simplified Object migrated to new flow - CC Txn")
    public void testJsCheckoutOldFlow_6digitBin(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_6DIGIT_BIN;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout for 8 digit Bin offers, simplified Object migrated to new flow - CC Txn")
    public void testJsCheckoutOldFlow_8digitBin(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "400";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_8DIGIT_BIN;

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout with CC with offer and PCF , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_CC_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
    @Test(description = "Js Checkout with Saved Card with offer and PCF , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMISavedCard_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        PaymentDTO paymentDTO = new PaymentDTO();
        String CustId = "EMITEST"+CommonHelpers.generateOrderId();
        SavedCardHelpers.addCardOnMidCustId(mid, CustId,paymentDTO.getExpMonth(), PaymentDTO.COFT_VISA_YEAR_EXPIRY, paymentDTO.getEmiCard());
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid)
                .setTxnValue(txnAmount)
                .setCustId(CustId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout with NB with offer and PCF , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_NB_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.payByNB(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads("150");
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
//    @Test(description = "Js Checkout with UPI with offer and PCF , simplified Object migrated to new flow - Bank offers Applied, Amount Based")
//    public void testJsCheckoutOldFlow_UPI_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
//        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
//        String txnAmount = "800";
//        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
//        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
//        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
//        InitTxnDTO initTxnDTO = new InitTxnDTO.
//                Builder(user.ssoToken(), mid)
//                .setTxnValue(txnAmount)
//                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
//                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
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
//        Assertions.assertThat(Float.parseFloat(txnStatus.getResponse().getChargeAmount())).isEqualTo(new Float(0.00));
//        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
//        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
//    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Test(description = "Js Checkout with EMI with offer and PCF , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout with EMI_DC with offer and PCF , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_DC_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        String custId = "MOCKGC10001";
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, mid)
                .setTxnValue(txnAmount)
                .setCustId(custId)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "Js Checkout with EMI Bank Flow with offer and PCF , simplified Object migrated to new flow - Subvention and Bank offers Applied, Amount Based")
    public void testJsCheckoutOldFlow_EMI_Bank_Flow_with_Offer_and_PCF(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "800";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_PCF;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, txnAmount, null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Feature("PGP-58212")
    @Test(description = "Verify that for NB paymode offerApply is not called when unifiedOffers does not have NETBANKING_LINKED offers")
    public void testJsCheckoutOldFlow_UnifiedOffers_Empty_NB(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "470";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_RESTRICT_OFFER;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"NB");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.payByNB(cashierPage, paymentDTO);
        cashierPage.pause(2);
        String reqresp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(reqresp).contains("/theia/api/v1/initiateTransaction").contains("/theia/api/v1/processTransaction");
        Assertions.assertThat(reqresp).doesNotContain("/theia/api/v1/offerApply");
    }

    @Owner(MEHUL_GUPTA)
    @Parameters({"JsType"})
    @Feature("PGP-58212")
    @Test(description = "Verify that for UPI paymode offerApply is not called when unifiedOffers does not have UPI_LINKED offers")
    public void testJsCheckoutOldFlow_UnifiedOffers_Empty_UPI(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "270";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_RESTRICT_OFFER;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(null).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken,mid,"UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payByUPI(cashierPage, paymentDTO);
        cashierPage.pause(2);
        String reqresp = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(reqresp).contains("/theia/api/v1/processTransaction");
        Assertions.assertThat(reqresp).doesNotContain("/theia/api/v1/offerApply");
    }

    @Owner(LOKESH_SAXENA)
    @Parameters({"JsType"})
    @Test(description = "JS Checkout for old flow with EMI Credit Card, EmiOfferDetailList should be visible for  Amount Based")
    public void testJsCheckoutOldFlow_ForEmiOfferDetailList_AmountBased_EmiCC(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "500";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        OfferDetails offerDetails = new OfferDetails();
        offerDetails.setOfferId("2434288");
        
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("2025030414210900020164341", null, txnAmount, offerDetails);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid, simplifiedSubvention)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .setCustId("EMITEST6a38eb6e7174454d85bf2e310525f21e")
                .build();
                
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO, false);
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
    @Test(description = "JS Checkout for old flow with Saved Card, EmiOfferDetailList should be visible for  Amount Based")
    public void testJsCheckoutOldFlow_ForEmiOfferDetailList_AmountBased_SavedCard(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "500";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        OfferDetails offerDetails = new OfferDetails();
        offerDetails.setOfferId("2434288");
        
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("2025030414210900020164341", null, txnAmount, offerDetails);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid, simplifiedSubvention)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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
    @Test(description = "JS Checkout for old flow with Bank Flow, EmiOfferDetailList should be visible for  Amount Based")
    public void testJsCheckoutOldFlow_ForEmiOfferDetailList_AmountBased_BankFlow(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "500";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        OfferDetails offerDetails = new OfferDetails();
        offerDetails.setOfferId("2434288");
        
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("2025030414210900020164341", null, txnAmount, offerDetails);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid, simplifiedSubvention)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .setCustId("EMITEST6a38eb6e7174454d85bf2e310525f21e")
                .build();
                
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMIBankFlow(cashierPage, paymentDTO, false);
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
    @Test(description = "JS Checkout for old flow with EMI Credit Card, EmiOfferDetailList should be visible for  Item Based")
    public void testJsCheckoutOldFlow_ForEmiOfferDetailList_ItemBased_EMICC(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        OfferDetails offerDetails = new OfferDetails();
        offerDetails.setOfferId("2432015");
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("15031116688","123047","18084",Arrays.asList("6226"),"1","1100.00");
        itemSub1.setOfferDetails(offerDetails);
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("2025030414210900020164341",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid, simplifiedSubvention)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .setCustId("EMITEST6a38eb6e7174454d85bf2e310525f21e")
                .build();
                
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO, false);
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
    @Test(description = "JS Checkout for new flow with Bank Flow, EmiOfferDetailList should be visible for Item Based")
    public void testJsCheckoutOldFlow_ForEmiOfferDetailList_ItemBased_BankFlow(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        OfferDetails offerDetails = new OfferDetails();
        offerDetails.setOfferId("2432015");
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("15031116688","123047","18084",Arrays.asList("6226"),"1","1100.00");
        itemSub1.setOfferDetails(offerDetails);
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("2025030414210900020164341",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid, simplifiedSubvention)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
                .setCustId("EMITEST6a38eb6e7174454d85bf2e310525f21e")
                .build();
                
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = openJs(JsType, initTxnDTO, txnToken, mid, "EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payByEMIBankFlow(cashierPage, paymentDTO, false);
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
    @Test(description = "JS Checkout for old flow with Saved Card, EmiOfferDetailList should be visible for Item Based")
    public void testJsCheckoutOldFlow_ForEmiOfferDetailList_ItemBased_SavedCard(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String txnAmount = "1100";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        OfferDetails offerDetails = new OfferDetails();
        offerDetails.setOfferId("2432015");
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item itemSub1 = new SimplifiedSubvention.Item("15031116688","123047","18084",Arrays.asList("6226"),"1","1100.00");
        itemSub1.setOfferDetails(offerDetails);
        items.add(itemSub1);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("2025030414210900020164341",null,items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), mid, simplifiedSubvention)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedSubvention(simplifiedSubvention)
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


}
