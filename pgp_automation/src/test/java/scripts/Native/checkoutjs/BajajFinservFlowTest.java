package scripts.Native.checkoutjs;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.MerchantType.BAJAJFN_CARDLESS;
import static com.paytm.appconstants.Constants.MerchantType.EMI_DISCOVERY;
import static com.paytm.appconstants.Constants.Owner.LOKESH_SAXENA;
import static com.paytm.appconstants.Constants.Owner.PUSPA;
import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static io.restassured.RestAssured.given;

public class BajajFinservFlowTest extends PGPBaseTest {

    // AI-Generated: 2024-12-19 - Global final constant for transaction amount
    private static final String TXN_AMOUNT = "10005";

    private final CheckoutJsCheckoutPage checkoutPage =new CheckoutJsCheckoutPage();
    private final CheckoutPage checkoutPagenew = new CheckoutPage();


    public void payByCardlessCard(CashierPage cashierPage,PaymentDTO paymentDTO){
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        cashierPage.payButton().click();
    }

    public void payByCardlessCardEnablePaymentMode(CashierPage cashierPage,PaymentDTO paymentDTO){
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        cashierPage.payButton().click();
    }


    public void payByCardlessCardBankFlow(CashierPage cashierPage,PaymentDTO paymentDTO){
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.clickonBajaj().click();

        cashierPage.waitUntilAllAJAXCallsFinish();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        DriverManager.getDriver().switchTo().parentFrame();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();

    }
    public void payByCardlessCardBankFlowwithmoreBanks(CashierPage cashierPage,PaymentDTO paymentDTO){
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().click();
        cashierPage.waitUntilLoads();
        cashierPage.tabNBFCCardless().assertVisible();
        cashierPage.tabNBFCCardless().click();
        cashierPage.clickEmiCardless().click();

        cashierPage.waitUntilAllAJAXCallsFinish();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        DriverManager.getDriver().switchTo().parentFrame();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();

    }

    @Owner(PUSPA)
    @Feature("PPSL-617")
    @Parameters({"theme"})
    @Test(description = "Verify Bajaj FN emi plans in FPO response when simplifiedUnifiedOffers object is passed")
    public void verifyBajajFNInFPO(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", TXN_AMOUNT, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails")).contains("BAJAJFN");
    }







    @Owner(PUSPA)
    @Feature("PPSL-617")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless with No offer Intent")
    public void E2ETxnwithBajaj(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.other_options[4].paymentMode")).contains("EMI_CARDLESS");

    }

    @Owner(PUSPA)
    @Feature("PPSL-617")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless with Multi Item Based")
    public void E2ETxnwithBajajItemBased(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;


        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 5002.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 5003.00, "78225");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails")).contains("BAJAJFN");


    }




    //Js on Redirection Flow

    @Owner(PUSPA)
    @Feature("PPSL-617")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless with Amount Based")
    public void E2ETxnwithBajajAmountBased_JsOnRedirection(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;

        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", TXN_AMOUNT, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();

        checkoutPagenew.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails")).contains("BAJAJFN");



    }

    @Owner(PUSPA)
    @Feature("PPSL-617")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless with Multi Item Based")
    public void E2ETxnwithBajajItemBased_JsOnRedirection(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;


        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 5002.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 5003.00, "78225");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();

        checkoutPagenew.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails")).contains("BAJAJFN");

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-58997")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless - Enable Payment Mode EMI_CARDLESS")
    public void E2ETxnwithBajajEnablePaymentMode_EMI_CARDLESS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;

        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI_CARDLESS", null);

        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", TXN_AMOUNT, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIPaymentMode})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.BAJAJFN_CARDLESS_CARD);

        cashierPage.waitUntilLoads();
        payByCardlessCardEnablePaymentMode(cashierPage,paymentDTO);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(merchant.getId())
                .validatePaymentMode("EMI_CARDLESS")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(TXN_AMOUNT)
                .AssertAll();

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-58997")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless - Enable Payment Mode EMI_CARDLESS BAJAJFN")
    public void E2ETxnwithBajajEnablePaymentMode_EMI_CARDLESS_BAJAJFN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;

        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI_CARDLESS", new String[]{"BAJAJFN"});

        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", TXN_AMOUNT, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIPaymentMode})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.BAJAJFN_CARDLESS_CARD);

        cashierPage.waitUntilLoads();
        payByCardlessCardEnablePaymentMode(cashierPage,paymentDTO);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(merchant.getId())
                .validatePaymentMode("EMI_CARDLESS")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(TXN_AMOUNT)
                .AssertAll();

    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-58997")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Transaction with Bajaj FN Cardless - Enable Payment Mode EMI_CARDLESS BAJAJFN and EMI HDFC")
    public void E2ETxnwithBajajEnablePaymentMode_EMI_CARDLESS_BAJAJFN_EMI_HDFC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant =BAJAJFN_CARDLESS;

        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        EnablePaymentMode enableEMIPaymentModeCardless = new EnablePaymentMode(null, "EMI_CARDLESS", new String[]{"BAJAJFN"});
        EnablePaymentMode enableEMIPaymentModeHDFC = new EnablePaymentMode(null, "EMI", new String[]{"HDFC"});

        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", TXN_AMOUNT, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(TXN_AMOUNT)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIPaymentModeCardless,enableEMIPaymentModeHDFC})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.BAJAJFN_CARDLESS_CARD);

        cashierPage.waitUntilLoads();
        payByCardlessCardEnablePaymentMode(cashierPage,paymentDTO);

        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(merchant.getId())
                .validatePaymentMode("EMI_CARDLESS")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(TXN_AMOUNT)
                .AssertAll();

    }

}
