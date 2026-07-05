package scripts.Promo.ORRFlow;

import com.paytm.LocalConfig;
import com.paytm.api.theia.FetchCardDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.dto.PaymentDTO.PROMO_CC_CARD_HDFC;

@Owner("Bharat Gandhi")
@Feature("PGP-29683")
public class FetchCardDetailsSCM extends PGPBaseTest {
    @Test
    public void validateFetchCardDetails_SCM_with_CIN() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_HDFC);
        Response bin8hashres = SavedCardHelpers.fetchCardsAlipay(user);
        String bin8hash = bin8hashres.jsonPath().getString("assetInfos.CC.extendInfo.eightDigitBinHash").replace("[", "").replace("]", "");

        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;

        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setToken(token)
                .setTokenType("SSO")
                .setMID(promoMerchant.getId())
                .setCardNumber("")
                .setSavedCardId(cin)
                .build();
        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, CommonHelpers.generateOrderId());
        fetchCardDetails.setContext("body.eightDigitBinRequired", true);
        fetchCardDetails.deleteContext("body.isEightDigitBinRequired");
        JsonPath response = fetchCardDetails.execute().jsonPath();

        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.cardHash")).isEqualTo(cin);
        Assertions.assertThat(response.getString("body.cardDetails.binDetail.bin")).isEqualTo(bin8hash);
    }

    @Test
    public void validateFetchCardDetails_SCM_with_New_Card() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");

        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PROMO_CC_CARD_HDFC);

        String cardIndexNumber = PGPHelpers.getCIN(paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setToken(token)
                .setTokenType("SSO")
                .setMID(promoMerchant.getId())
                .setCardNumber(PROMO_CC_CARD_HDFC)
                .setSavedCardId("")
                .build();
        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, CommonHelpers.generateOrderId());
        fetchCardDetails.setContext("body.eightDigitBinRequired", true);
        fetchCardDetails.deleteContext("body.isEightDigitBinRequired");

        JsonPath response = fetchCardDetails.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.cardHash")).isEqualTo(cardIndexNumber);
        Assertions.assertThat(response.getString("body.cardDetails.binDetail.bin")).isNotEmpty();
    }
}