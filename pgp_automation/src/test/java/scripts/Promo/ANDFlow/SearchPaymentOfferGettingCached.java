package scripts.Promo.ANDFlow;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;
import scripts.LogValidationRetryAnalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Owner("Abhishek Gupta")
@Feature("PGP-34765")
public class SearchPaymentOfferGettingCached extends PGPBaseTest{

    String version = "v1/promosearch/payment";

    @Owner("Abhishek Gupta")
    @Test(description = "Validate promo search call once", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_CallPromoSearchOnce() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.NATIVE_HYBRID;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"REQUEST\" | wc -l \n";
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);

        String grepPromoSearchResponse = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"RESPONSE\" | grep \"" + promocode.getName() + "\" \n";
        String theiafacadelogsPromoSearchResponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchResponse);
        Assertions.assertThat(theiafacadelogsPromoSearchResponse).contains("promocode", promocode.getName());

        ApiV1ApplyPromo promo1 = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson1 = promo1.execute().jsonPath();

        Assertions.assertThat(applyPromoJson1.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson1.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepTheiaLog = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia.log |grep \"Serving SearchPaymentOffersServiceResponse from cache\"   \n";
        String theialogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepTheiaLog);
        Assertions.assertThat(theialogsPromoSearchRequest).contains(promoMerchant.getId());
    }


    @Owner("Abhishek Gupta")
    @Test(description = "Validate promo search call once End to End Flow", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_CallPromoSearchOnceEndToEndFlow() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.Addnpay;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        HashMap<String, Object> offer = new HashMap<>();
        offer.put("promocodeApplied",promocode.getName());
        offer.put("promotext","good promo");
        offer.put("instantDiscount",null);
        offer.put("cashbackAmount","5.00");
        offer.put("payMethod",paymentOptions.getPayMethod());
        offer.put("promoVisibility","false");
        offer.put("responseCode",null);
        offer.put("transactionAmount",paymentOptions.getTransactionAmount());
        offer.put("paytmCashbackAmount",null);

        List<HashMap<String, Object>> offerBreakupList1 = new ArrayList<>();
        offerBreakupList1.add(offer);

        HashMap <String, Object> hm = new HashMap<>();
        hm.put("totalInstantDiscount", null);
        hm.put("totalCashbackAmount", "5.00");
        hm.put("offerBreakup", offerBreakupList1);
        hm.put("totalTransactionAmount",paymentOptions.getTransactionAmount());
        hm.put("totalPaytmCashbackAmount",null);

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(hm);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token,promoMerchant, paymentOffersApplied).build();

        String body = initTxnDTO.getBody().toString();
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + paymentDTO.PROMO_CC_CARD_HDFC + "|123|082026")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assert.assertEquals(processTxnV1Response.getBody().getResultInfo().getResultMsg(),"Success");

        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | wc -l  \n" ;
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);
        String grepPromoSearchResponse = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\"| grep \"RESPONSE\" | grep \"" + promocode.getName() + "\" \n" ;
        String theiafacadelogsPromoSearchResponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchResponse);
        Assertions.assertThat(theiafacadelogsPromoSearchResponse).contains("promocode", promocode.getName());
    }


    @Owner("Abhishek Gupta")
    @Test(description = "Validate promo search not call in second time", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_PromoSearchNotCallSecondTime() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.PGOnly_NativeOldFlow;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"REQUEST\" | wc -l \n" ;
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);

        String grepPromoSearchResponse = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"RESPONSE\" | grep \"" + promocode.getName() + "\" \n" ;
        String theiafacadelogsPromoSearchResponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchResponse);
        Assertions.assertThat(theiafacadelogsPromoSearchResponse).contains("promocode", promocode.getName());

        ApiV1ApplyPromo promo1 = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson1 = promo1.execute().jsonPath();

        Assertions.assertThat(applyPromoJson1.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson1.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepTheiaLog = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia.log |grep \"Serving SearchPaymentOffersServiceResponse from cache\"   \n";
        String theialogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepTheiaLog);
        Assertions.assertThat(theialogsPromoSearchRequest).contains(promoMerchant.getId());
    }


    @Owner("Abhishek Gupta")
    @Test(description = "Validate promo search call more than one time with in 15 min", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_PromoSearchCallMoreThanOneTime() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid_S;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForRead(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"REQUEST\" | wc -l \n" ;
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);
        String grepPromoSearchResponse = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"RESPONSE\" | grep \"" + promocode.getName() + "\" \n" ;
        String theiafacadelogsPromoSearchResponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchResponse);
        Assertions.assertThat(theiafacadelogsPromoSearchResponse).contains("promocode", promocode.getName());

        ApiV1ApplyPromo promo1 = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson1 = promo1.execute().jsonPath();

        Assertions.assertThat(applyPromoJson1.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson1.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest1 = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"REQUEST\" | wc -l \n" ;
        String theiafacadelogsPromoSearchRequest1 = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest1);

        String grepPromoSearchResponse1 = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \" "+version+"\" | grep \"RESPONSE\" | grep \"" + promocode.getName() + "\" \n" ;
        String theiafacadelogsPromoSearchResponse1 = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchResponse1);
        Assertions.assertThat(theiafacadelogsPromoSearchResponse1).contains("promocode", promocode.getName());
    }

    private ApiV1ApplyPromo getApiV1ApplyPromo(Constants.MerchantType promoMerchant, String token, Promo promocode, PaymentOptions paymentOptions) {
        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(token)
                .setMID(promoMerchant.getId())
                .setPromocode(promocode.getName())
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ApiV1ApplyPromo promo = new ApiV1ApplyPromo(applyPromoDTO);
        return promo;
    }
}

