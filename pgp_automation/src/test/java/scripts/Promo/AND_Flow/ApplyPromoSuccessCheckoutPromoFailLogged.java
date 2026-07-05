package scripts.Promo.AND_Flow;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import scripts.LogValidationRetryAnalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Owner("Abhishek Gupta")
@Feature("PGP-34822")
public class ApplyPromoSuccessCheckoutPromoFailLogged extends PGPBaseTest{

    @Owner("Abhishek Gupta")
    @Test(description = "Verify that apply promo API is successful and checkout promo is unsuccessful and doing txn Credit Card", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_ApplyPromoSuccessAndCheckoutFailCC() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);

        Promo promocode = new Promo(true);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("1.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        HashMap<String, Object> offer = new HashMap<>();
        offer.put("promocodeApplied","failure");
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

        System.out.println(hm);

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(hm);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token,promoMerchant, paymentOffersApplied).build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
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

        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("The offer you are trying to redeem is not valid");
        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/events.log |grep \"CHECKOUT_PROMO_FAILED\"   \n" ;
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);
        Assertions.assertThat((theiafacadelogsPromoSearchRequest).contains(promoMerchant.getId())).isTrue();

    }

    @Owner("Abhishek Gupta")
    @Test(description = "Verify that apply promo API is successful and checkout promo is unsuccessful and doing txn Debit Card", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_ApplyPromoSuccessAndCheckoutFailDC() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);

        Promo promocode = new Promo(true);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("1.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        HashMap<String, Object> offer = new HashMap<>();
        offer.put("promocodeApplied","failure");
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

        System.out.println(hm);

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(hm);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token,promoMerchant, paymentOffersApplied).build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + paymentDTO.DC + "|123|082026")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("The offer you are trying to redeem is not valid");
        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/events.log |grep \"CHECKOUT_PROMO_FAILED\"   \n" ;
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);
        Assertions.assertThat((theiafacadelogsPromoSearchRequest).contains(promoMerchant.getId())).isTrue();
    }

    @Owner("Abhishek Gupta")
    @Test(description = "Verify that apply promo API is successful and checkout promo is unsuccessful and doing txn Credit Card", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_ApplyPromoSuccessAndCheckoutFailCC1() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);

        Promo promocode = new Promo(true);

        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("1.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        ApiV1ApplyPromo promo = getApiV1ApplyPromo(promoMerchant, token, promocode, paymentOptions);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        HashMap<String, Object> offer = new HashMap<>();
        offer.put("promocodeApplied","failure");
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

        System.out.println(hm);

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(hm);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token,promoMerchant, paymentOffersApplied).build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + paymentDTO.VISA_CREDIT_CARD_NUMBER + "|123|082026")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("The offer you are trying to redeem is not valid");
        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepPromoSearchRequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/events.log |grep \"CHECKOUT_PROMO_FAILED\"   \n" ;
        String theiafacadelogsPromoSearchRequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepPromoSearchRequest);
        Assertions.assertThat((theiafacadelogsPromoSearchRequest).contains(promoMerchant.getId())).isTrue();
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


