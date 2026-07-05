package scripts.Promo.ANDFlow;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.ApplyPromoDTO;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import scripts.LogValidationRetryAnalyser;

@Owner("Bharat Gandhi")
@Feature("PGP-29683")
public class ApplyPromoSCM extends PGPBaseTest {
    @Test(description = "Validate apply promo with CIN", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_UsingCIN() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_HDFC);

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);

        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "HDFC", "", cin, null);

        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(token)
                .setMID(promoMerchant.getId())
                .setPromocode(promocode.getName())
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ApiV1ApplyPromo promo = new ApiV1ApplyPromo(applyPromoDTO);
        JsonPath applyPromoJson = promo.execute().jsonPath();

        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultStatus")).describedAs("Config name doesn't match").isEqualTo("S");
        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepcmdapplypromorequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/apply\"|grep \"REQUEST\"| grep \"" + promocode.getName() + "\" |grep " + cin + "\n";
        String theiafacadelogsapplypromorequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdapplypromorequest);
        Assertions.assertThat(theiafacadelogsapplypromorequest).contains("cardIndexNo", cin);

        String grepcmdapplypromoresponse = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/apply\"| grep \"RESPONSE\"|grep \"" + promocode.getName() + "\" \n";
        String theiafacadelogsapplypromoresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdapplypromoresponse);
        Assertions.assertThat(theiafacadelogsapplypromoresponse).contains(promocode.getName());


    }

    @Test(description = "Validate apply promo with New Card", retryAnalyzer = LogValidationRetryAnalyser.class)
    public void validateApplyPromo_UsingNewCard() throws Exception {
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        //SoftAssert softAssert = new SoftAssert();

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("100.0", "CREDIT_CARD", "HDFC", paymentDTO.PROMO_CC_CARD_HDFC, "", null);

        String cin = PGPHelpers.getCIN(paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_HDFC);

        ApplyPromoDTO applyPromoDTO = new ApplyPromoDTO.Builder()
                .setTokenType("SSO")
                .setToken(token)
                .setMID(promoMerchant.getId())
                .setPromocode(promocode.getName())
                .setPaymentOptions(new PaymentOptions[]{paymentOptions})
                .setTotalTransactionAmount("100.0")
                .build();
        ApiV1ApplyPromo promo = new ApiV1ApplyPromo(applyPromoDTO);
        JsonPath applyPromoJson = promo.execute().jsonPath();
        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultStatus")).describedAs("Config name doesn't match").isEqualTo("S");
        Assertions.assertThat(applyPromoJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(applyPromoJson.getString("body.paymentOffer.offerBreakup.promocodeApplied")).contains(promocode.getName());

        String grepcmdapplypromorequest = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/apply\"|grep \"REQUEST\"| grep \"" + promocode.getName() + "\" |grep " + cin + "\n";
        String theiafacadelogsapplypromorequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdapplypromorequest);
        Assertions.assertThat(theiafacadelogsapplypromorequest).contains("cardIndexNo", cin);

        String grepcmdapplypromoresponse = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/apply\"| grep \"RESPONSE\"|grep \"" + promocode.getName() + "\" \n";
        String theiafacadelogsapplypromoresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdapplypromoresponse);
        Assertions.assertThat(theiafacadelogsapplypromoresponse).contains(promocode.getName());


    }
}
