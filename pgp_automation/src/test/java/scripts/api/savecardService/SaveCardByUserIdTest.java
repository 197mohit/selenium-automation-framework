package scripts.api.savecardService;

import com.paytm.LocalConfig;
import com.paytm.api.saveCard.SavedCardByUserId;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Sprint-31.1")
@Owner("Deepak")
@Owners(author = "Deepak", qa = "Karmvir")
@Feature("PGP-18000")
public class SaveCardByUserIdTest extends PGPBaseTest {

   private PaymentDTO paymentDTO = new PaymentDTO();


    @Test(description = "to test all the saved cards should be fetched for valid user id and card index is true")
    public void savedCardForValidUserWithCardIndexNumber() throws Exception {
        User user= userManager.getForWrite(Label.BASIC);
        String userId = user.custId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType","JWT");
        tokenMap.put("userId",userId);
        String JWTToken = PGPHelpers.createJsonWebToken(tokenMap,PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(JWTToken,userId,"true");
        JsonPath savedJson = savedCardByUserId.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Status is not success").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.savedCardDetails[0].cardId")).as("Card is not saved for this user").isNotEmpty();
        Assertions.assertThat(savedJson.getString("body.savedCardDetails[0].maskedCardNumber")).as("Card number is not masked").contains("XXXXXX");
    Assertions.assertThat(savedJson.getString("body.savedCardDetails[0].cardIndexNumber")).as("cardIndexNumber is empty").isNotEmpty();
    }

    @Test(description = "to test all the saved cards should be fetched for valid user id and card index is false")
    public void savedCardForValidUserWithoutCardIndexNumber() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String userId = user.custId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType", "JWT");
        tokenMap.put("userId", userId);
        String JWTToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(JWTToken, userId, "false");
        JsonPath savedJson = savedCardByUserId.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Status is not success").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.savedCardDetails[0].cardId")).as("Card is not saved for this user").isNotEmpty();
        Assertions.assertThat(savedJson.getString("body.savedCardDetails[0].maskedCardNumber")).as("Card number is not masked").contains("XXXXXX");
    }
    @Test(description = "to test that card should not be fetched for invalid user id and card index is true")
    public void savedCardForInvalidUser() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String invalidUserId = "111111";
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType", "JWT");
        tokenMap.put("userId", invalidUserId);
        String JWTToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(JWTToken, invalidUserId, "false");
        JsonPath savedJson = savedCardByUserId.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Status is not success").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Cards are exits on this user").isEqualTo("Card does not exist for given parameters");
    }
    @Test(description = "to test that saved cards should not fetched for User which is not having saved card")
    public void savedCardForUserNotHavingSavedCard() throws Exception {
        User user= userManager.getForWrite(Label.BASIC);
        String userId = user.custId();
        SavedCardHelpers.deleteSavedCard(user);
        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType","JWT");
        tokenMap.put("userId",userId);
        String JWTToken = PGPHelpers.createJsonWebToken(tokenMap,PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(JWTToken,userId,"true");
        JsonPath savedJson = savedCardByUserId.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Status is not success").isEqualTo("S");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("Cards are exits on this user").isEqualTo("Card does not exist for given parameters");
    }
    @Test(description = "to test that JWT token validation fail")
    public void SavedCardForUserWithInavlidJWTToken() throws Exception {
        User user= userManager.getForWrite(Label.BASIC);
        String userId = user.custId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType","JWT");
        tokenMap.put("UserId",userId);
        String JWTToken = PGPHelpers.createJsonWebToken(tokenMap,PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(JWTToken,userId,"true");
        JsonPath savedJson = savedCardByUserId.execute().jsonPath();
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultStatus")).as("Status is not success").isEqualTo("F");
        Assertions.assertThat(savedJson.getString("body.resultInfo.resultMsg")).as("JWT token is Valid").isEqualTo("Invalid data entered by user");
    }
}
