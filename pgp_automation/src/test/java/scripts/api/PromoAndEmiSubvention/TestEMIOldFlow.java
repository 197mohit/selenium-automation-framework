package scripts.api.PromoAndEmiSubvention;

import com.paytm.api.TxnStatus;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItems;

public class TestEMIOldFlow extends PGPBaseTest {

    private static final String CARD_NO = "4895380115392363";
    private static final String CHANNEL_CODE = "HDFC";
    private static final String EMI_TYPE = "CREDIT_CARD";
    private static final String PLAN_ID = "HDFC|3";
    private final static ResponseSpecification SUCCESS_RESPONSE = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("s"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("0000"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Success"))
            .build();

    private final CheckoutJsCheckoutPage checkoutPage =new CheckoutJsCheckoutPage();

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Standard EMI Txn" )
    public void testDummyValuesNotBeingSentToATSforAmtBasedTxn_STANDARD() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        String transactionAmount="1000";
        TxnAmount txnAmount = new TxnAmount(transactionAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setTxnValue(transactionAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),"TXN_TOKEN",txnToken,initTxnDTO.orderFromBody().toString(),txnAmount.getValue())
                .setPaymentMode("EMI")
                .setCardInfo("|" + "4718650100010336" + "|545|122027")
                .setAuthMode("otp")
                .setChannelCode(CHANNEL_CODE)
                .setEmiType(EMI_TYPE)
                .setPlanId(PLAN_ID)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":false");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Item based Subvention and Amount Based Promo" )
    public void testDummyValuesNotBeingSentToATSforItembasedSubventionAmtBasedPromo_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        Promo promo = new Promo("PGQAEMITEST22");
        Merchant merchant = new Merchant(merchantType.getId(), true);
        merchant.getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setApplyAvailablePromo("true").setValidatePromo("false").setPromoCode("PGQAEMITEST22");
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("111120124","12311","5611", Arrays.asList("5511"),"1","800");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("800.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Item Based Subvention" )
    public void testDummyValuesNotBeingSentToATSforItembasedSubvention_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("111120126","12310011021","45603110",Arrays.asList("4560345110"),"1","1100");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("1100.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Amount Based Subvention" )
    public void testDummyValuesNotBeingSentToATSforAmtbasedSubvention_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), "800", true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("800.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Amount Based Promo and Amount Based Subvention" )
    public void testDummyValuesNotBeingSentToATSforAmtbasedSubventionAmtBasedPromo_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        Promo promo = new Promo("PGQAEMITEST22");
        Merchant merchant = new Merchant(merchantType.getId(), true);
        merchant.getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setApplyAvailablePromo("true").setValidatePromo("false").setPromoCode("PGQAEMITEST22");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), "800", true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setTxnValue("800.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Amount Based Promo" )
    public void testDummyValuesNotBeingSentToATSforAmtBasedPromo_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
//        Promo promo = new Promo("");
//        Merchant merchant = new Merchant(merchantType.getId(), true);
//        merchant.getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setApplyAvailablePromo("true").setValidatePromo("false").setPromoCode(null);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setTxnValue("800.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Item Based Promo" )
    public void testDummyValuesNotBeingSentToATSforItemBasedPromo_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        Promo promo = new Promo("PGQAEMITEST22");
        Merchant merchant = new Merchant(merchantType.getId(), true);
        merchant.getPromos().add(promo);
        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6224");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items items= new SimplifiedPaymentOffers.Items("113112","","1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setTxnValue("1100.00")
                //.setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode(new String[]{}, "EMI")})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        System.out.println(logsResponse);
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Item Based Promo and Amount Based Subvention" )
    public void testDummyValuesNotBeingSentToATSforItemBasedPromoAmtBasedSubvention_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        Promo promo = new Promo("PGQAEMITEST22");
        Merchant merchant = new Merchant(merchantType.getId(), true);
        merchant.getPromos().add(promo);
        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6224");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items items= new SimplifiedPaymentOffers.Items("113112","","1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), "1100", true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setTxnValue("1100.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        System.out.println(logsResponse);
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56079")
    @Parameters({"theme"})
    @Test(description = "Verify that Dummy Item values are not being sent in /ats/v2/order/checkout API in case of Simplified Old Flow - Item Based Promo and Item Based Subvention" )
    public void testDummyValuesNotBeingSentToATSforItemBasedPromoItemBasedSubvention_Simplified_Old(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(CARD_NO);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForWrite(PGPBaseTest.Label.EMIDCELIGIBLE);
        Promo promo = new Promo("PGQAEMITEST22");
        Merchant merchant = new Merchant(merchantType.getId(), true);
        merchant.getPromos().add(promo);
        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("6224");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123", null, "18084",categoryIds);
        SimplifiedPaymentOffers.Items items= new SimplifiedPaymentOffers.Items("113112","","1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","false",cartDetails);
        List<SimplifiedSubvention.Item> items2 = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("113112","123","18084",Arrays.asList("6224"),"1","1100");
        items2.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention(user.custId(), null, items2);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,simplifiedPaymentOffers)
                .setTxnValue("1100.00")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.viewAllOffersAvialable_HideButton().click();
        } catch (AssertionError e) {
            //
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "REQUEST");
        System.out.println(logsResponse);
        Assertions.assertThat(logsResponse).isNotEmpty();
        Assertions.assertThat(logsResponse).contains("\"isOfferCheckoutRequired\":true");
        Assertions.assertThat(logsResponse).doesNotContain("123456_idDummy");
        Assertions.assertThat(logsResponse).doesNotContain("EFGH_productNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_brandIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("ABCD_brandNameDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_categoryIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("123456_sellerIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("IJKL_modelDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationIdDummy");
        Assertions.assertThat(logsResponse).doesNotContain("12345_validationSourceDummy");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PPSL-598")
    @Test(description="Verify Old Flow Migrated to new Flow Custom Checkout Txn with 9 digit bin in ValidateEmi")
    public void testCustomCheckoutEMITxn_OldFlowMigratedToNewFlow_with9DigitBin() throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        String custid=user.custId();
        String transactionAmount="2000";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard("");
        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken("4769953850009925");
        cardTokenInfo.setTokenExpiry("122027");
        cardTokenInfo.setCardSuffix("2363");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013022106003487586699918");
        cardTokenInfo.setTokenType("ALTERNATE");
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(mid.getId())
                .setMerchantKey(mid.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .setItems(null)
                .setSubventionAmount(transactionAmount)
                .setPrice(transactionAmount)
                .build();
        ApiV1Tenure api = new ApiV1Tenure(mid.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();
        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        String offerId = tenureResp.getString("body.planDetails.find{it.planId == '"+planId+"'}.itemBreakUp[0].offerId");
        BigInteger intplanid= new BigInteger(planId);
        OfferDetails offerDetails = new OfferDetails().setOfferId(offerId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", mid.getId())
                .setMerchantKey(mid.getKey())
                .setPlanId(intplanid)
                .setItems(null)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(transactionAmount)
                                .setCardBin("489538011"))
                .setGenerateTokenForIntent(true)
                .setCacheCardToken("4769953850009925")
                .setPrice(transactionAmount)
                .setSubventionAmount(transactionAmount)
                .setOfferDetails(offerDetails)
                .build();
        ApiV1Validate api2 = new ApiV1Validate(mid.getId(), req2);
        Response r2 = api2.execute();
        r2.then()
                .spec(SUCCESS_RESPONSE);
        JsonPath jsonPath = r2.jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        double payableAmount = Double.valueOf(jsonPath.getString("body.finalTransactionAmount"));
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        String emiSubventionToken = jsonPath.getString("body.emiSubventionToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setEmiSubventionToken(emiSubventionToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(),txnToken,initTxnDTO.orderFromBody().toString())
                .setPaymentMode("EMI")
                .setCardInfo("||545|")
                .setAuthMode("otp")
                .setEmiType("CREDIT_CARD")
                .setChannelCode(CHANNEL_CODE)
                .setPlanId("HDFC|3")
                .setTxnAmount(null)
                .setcardTokenInfo(cardTokenInfo)
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(), "/ats/v2/order/checkout", "RESPONSE");
        Assertions.assertThat(logsResponse).contains("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        TxnStatus txnStatus = new TxnStatus(mid.getId(),initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
                validateStatus("TXN_SUCCESS").
                validatePaymentMode("EMI").AssertAll();
    }
}
