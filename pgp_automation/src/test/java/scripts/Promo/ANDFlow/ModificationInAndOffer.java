package scripts.Promo.ANDFlow;

import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import static com.paytm.appconstants.Constants.Owner.Abhishek_Gupta;

public class ModificationInAndOffer extends CheckoutJsBase {
    private final static String PG_DISCOUNT_PROMO_CODE = "discount";

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "Validate that when we hit fpo and checked userId pass over promosearch")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndUseridPassOverPromosearch(@Optional("Native+") String theme) throws Exception {
        Constants.MerchantType pwpDefault = Constants.MerchantType.AND_OFFER_MID;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(PG_DISCOUNT_PROMO_CODE).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getMid(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("paytm-user-id");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "Validate that when we hit fpo and checked ClientId pass over promosearch")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndClientIdPassOverPromosearch(@Optional("Native+") String theme) throws Exception {
        Constants.MerchantType pwpDefault = Constants.MerchantType.AND_OFFER_MID;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(PG_DISCOUNT_PROMO_CODE).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("X-CLIENT-ID");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "Validate that when we hit fpo and checked Mid-Level cache pass over promosearch")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndMidLevelcachePassOverPromosearchResponse(@Optional("Native+") String theme) throws Exception {
        Constants.MerchantType pwpDefault = Constants.MerchantType.AND_OFFER_MID;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(PG_DISCOUNT_PROMO_CODE).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("\"midLevelCache\":false");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "Validate that when we hit fpo and checked userId pass over applyBulk API")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndUseridPassOverApplybulkApi(@Optional("Native+") String theme) throws Exception {
        Constants.MerchantType pwpDefault = Constants.MerchantType.AND_OFFER_MID;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(PG_DISCOUNT_PROMO_CODE).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getMid(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("\"user\":{\"id\"");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "Validate that when we hit fpo and checked PaytmUserId pass over applyBulk API")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndPaytmUserIdPassOverApplybulkApi(@Optional("Native+") String theme) throws Exception {
        Constants.MerchantType pwpDefault = Constants.MerchantType.AND_OFFER_MID;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        for (int i = 0; i < 2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(PG_DISCOUNT_PROMO_CODE).setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v1").setFetchAllPaymentOffers("true").setApplyPaymentOffers("true").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getMid(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("paytmUserId");
        Assertions.assertThat(logs).contains("customer-id");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "validate for checkoutjs flow and checked userId pass over promosearch")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndUseridPassOverPromosearch1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AND_OFFER_MID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("paytm-user-id");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "validate for checkoutjs flow and checked ClientId pass over promosearch")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndClientIdPassOverPromosearch1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AND_OFFER_MID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getMid(), "PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("X-CLIENT-ID");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "validate for checkoutjs flow and checked Mid-Level cache pass over promosearch")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndMidLevelcachePassOverPromosearchResponse1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AND_OFFER_MID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "RESPONSE","PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("\"midLevelCache\":false");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "validate for checkoutjs flow and checked userId pass over applyBulk API")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndUseridPassOverApplybulkApi1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AND_OFFER_MID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("\"midLevelCache\":false");
    }

    @Owner(Abhishek_Gupta)
    @Parameters({"theme"})
    @Test(description = "validate for checkoutjs flow and checked PaytmUserId pass over applyBulk API")
    public void validateBankOffersSimplifiedFlowforSavedVpaAndPaytmUserIdPassOverApplybulkApi1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AND_OFFER_MID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs).contains("paytm-user-id");
        Assertions.assertThat(logs).contains("customer-id");
    }
}
