package scripts.coft.savedCardService;

import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.DeleteCardByMidCustId;
import com.paytm.api.coft.saveCard.SavedCardByMidCustId;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.dto.PaymentDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DeleteCardbyMidCustIdTest {
    Constants.MerchantType coftMerchant = Constants.MerchantType.COFT_MERCHANT_3P;
    public String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();

    SoftAssertions softly = new SoftAssertions();

    @BeforeSuite
    public void beforeSuiteCheck()
    {
        System.out.println("Checking ff4j");
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        System.out.println("Enabled FF4J flag returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        System.out.println("Enabled FF4J flag returnSavedCardsFromPlatformForUserId");
        FF4JFlags.enable("sc_returnSavedCardFromPlatformForUserId");
        System.out.println("Enabled FF4J flag sc_returnSavedCardFromPlatformForUserId");

    }

    @Test
    public void verifyValidSavedCardsByMidCustIdGetsDeleted() throws Exception {
        String mid = coftMerchant.getId();
        custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(coftMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        String savedCardJwt = SavedCardHelpersNew.createJsonWebTokenToSavedCard("JWT", mid, custId, "false",LocalConfig.AUTCOF25834699562781_JET_KEY);
        JsonPath savedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(savedCardJwt, mid, custId);
        String savedCardId = savedCardResponseJsonPath.getString("response.savedCardId[0]");
        String deleteCardJwt = SavedCardHelpersNew.createJsonWebTokenToDeleteCard("JWT", mid, custId, savedCardId,LocalConfig.AUTCOF25834699562781_JET_KEY);
        SavedCardHelpersNew.deleteAllCardsMidCustId(savedCardResponseJsonPath, deleteCardJwt, mid, custId);
        JsonPath afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(savedCardJwt, mid, custId);
        int savedCardListsize = afterCardDeleteSavedCardResponseJsonPath.getList("response").size();
        SoftAssertions softly = new SoftAssertions();
        int count=5;
        while(savedCardListsize!=0&&count>0)
        {
            afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(savedCardJwt, mid, custId);
            savedCardListsize = afterCardDeleteSavedCardResponseJsonPath.getList("response").size();
            System.out.println("savedCardListsize is "+savedCardListsize);
            count--;
        }
        softly.assertThat(savedCardListsize).isEqualTo(0);
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("responseStatus").equalsIgnoreCase("SUCCESS"));
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("httpSubCode").equalsIgnoreCase("204"));
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail").equalsIgnoreCase("Card does not exist for given parameters"));
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-42626")
    @Test(description = "Validate the successful transaction of Delete card api with jwt token where the key has created in vault")
    public void VerifySuccessFullTxnDeleteCardWhereJwtFromVault() throws Exception {
        JsonPath deleteCardResponse =null;
        SavedCardHelpers.addCardOnMidCustId(coftMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType", "JWT");
        tokenMap.put("mid", coftMerchant.getId());
        tokenMap.put("custId", custId);
        tokenMap.put("filterTokenCards", "false");
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(), custId, false, "JWT", jwt);
        JsonPath savedCardResponse = savedCards.execute().jsonPath();
        Map<String, String> jwtMap = new HashMap<>();
        jwtMap.put("mid", coftMerchant.getId());
        jwtMap.put("custId", custId);
        jwtMap.put("cardId", savedCardResponse.getString("response.savedCardId[0]"));
        jwtMap.put("requestedBy", "CARDHOLDER");
        jwtMap.put("reason", "Customer wants to delete the token");
        jwtMap.put("reasonCode", "CUSTOMER_CONFIRMED");
        String jwtDelete = PGPHelpers.createJsonWebToken(jwtMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        List<Object> list = savedCardResponse.getList("response");
        int size = list.size();
        System.out.println("size is" + size);
        //deleting cards if exist
        while (size != 0) {
            String savedCardId = savedCardResponse.getString("response.savedCardId[" + (size - 1) + "]");
            DeleteCardByMidCustId deleteCard = new DeleteCardByMidCustId(coftMerchant.getId(), custId).buildRequest(coftMerchant.getId(), custId, savedCardId, "JWT", jwtDelete);
             deleteCardResponse = deleteCard.execute().jsonPath();
            System.out.println(savedCardId + " Deleted");
            size--;
        }
        softly.assertThat(deleteCardResponse.getString("resultStatus")).isEqualTo("SUCCESS");
        softly.assertThat(deleteCardResponse.getString("resultMsg")).isEqualTo("Card deactivated successfully");
        softly.assertAll();
        JsonPath afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(jwt, coftMerchant.getId(), custId);
        int savedCardListsize = afterCardDeleteSavedCardResponseJsonPath.getList("response").size();
        String codeDetail=afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail");
        int count=5;
        while (count>0&&codeDetail.equalsIgnoreCase("SUCCESS"))
        {
            afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(jwt, coftMerchant.getId(), custId);
            codeDetail=afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail");
            savedCardListsize = afterCardDeleteSavedCardResponseJsonPath.getList("response").size();
            count--;
        }
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardListsize).isEqualTo(0);
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail")).isEqualTo("Card does not exist for given parameters");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-42626")
    @Test(description = "Validate the transaction of Delete card api with invalid jwt token")
    public void VerifyDeleteCardDetailsWithInvalidJwtFromVault() throws Exception {
        JsonPath deleteCardResponse =null;
        SavedCardHelpers.addCardOnMidCustId(coftMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType", "JWT");
        tokenMap.put("mid", coftMerchant.getId());

        tokenMap.put("custId", custId);
        tokenMap.put("filterTokenCards", "false");
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(), custId, false, "JWT", jwt);
        JsonPath savedCardResponse = savedCards.execute().jsonPath();
        Map<String, String> jwtMap = new HashMap<>();
        jwtMap.put("mid", coftMerchant.getId());
        jwtMap.put("custId", custId);
        jwtMap.put("cardId", savedCardResponse.getString("response.savedCardId[0]"));
        jwtMap.put("requestedBy", "CARDHOLDER");
        jwtMap.put("reason", "Customer wants to delete the token");
        jwtMap.put("reasonCode", "CUSTOMER_CONFIRMED");
        String jwtDelete = PGPHelpers.createJsonWebToken(jwtMap, PGPHelpers.ISSUER.ts, LocalConfig.INVALID_VAULT_JWT_KEY);
        List<Object> list = savedCardResponse.getList("response");
        int size = list.size();
        System.out.println("size is" + size);
        //deleting cards if exist
        while (size != 0) {
            String savedCardId = savedCardResponse.getString("response.savedCardId[" + (size - 1) + "]");
            DeleteCardByMidCustId deleteCard = new DeleteCardByMidCustId(coftMerchant.getId(), custId).buildRequest(coftMerchant.getId(), custId, savedCardId, "JWT", jwtDelete);
            deleteCardResponse = deleteCard.execute().jsonPath();
            System.out.println(savedCardId + " Deleted");
            size--;
        }
        softly.assertThat(deleteCardResponse.getString("resultStatus")).isEqualTo("FAIL");
        softly.assertThat(deleteCardResponse.getString("resultMsg")).isEqualTo("Jwt Validation Failure");
        softly.assertAll();
        JsonPath afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(jwt, coftMerchant.getId(), custId);
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardResponse.getString("response.savedCardId[0]")).isEqualTo(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-42626")
    @Test(description = "Validate the successful transaction of Delete card api with checksum for Tokenise cards")
    public void VerifySuccessFullTxnDeleteCardWithChecksum() throws Exception {
        JsonPath deleteCardResponse =null;
        SavedCardHelpers.addCardOnMidCustId(coftMerchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("tokenType", "JWT");
        tokenMap.put("mid", coftMerchant.getId());
        tokenMap.put("custId", custId);
        tokenMap.put("filterTokenCards", "false");
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(), custId, false, "JWT", jwt);
        JsonPath savedCardResponse = savedCards.execute().jsonPath();
        TreeMap<String,String> treeMap = new TreeMap();
        treeMap.put("mid",coftMerchant.getId());
        treeMap.put("custId",custId);
        treeMap.put("requestedBy","CARDHOLDER");
        treeMap.put("reason","Customer wants to delete the token");
        treeMap.put("reasonCode","CUSTOMER_CONFIRMED");
        List<Object> list = savedCardResponse.getList("response");
        int size = list.size();
        System.out.println("size is" + size);
        //deleting cards if exist
        while (size != 0) {
            String savedCardId = savedCardResponse.getString("response.savedCardId[" + (size - 1) + "]");
            treeMap.put("cardId",savedCardId);
            String token = PGPUtil.getChecksum(coftMerchant.getKey(), treeMap);
            DeleteCardByMidCustId deleteCard =new DeleteCardByMidCustId(coftMerchant.getId(), custId).buildRequest(coftMerchant.getId(),custId,savedCardId,"CHECKSUM",token);
             deleteCardResponse = deleteCard.execute().jsonPath();
            System.out.println(savedCardId + " Deleted");
            softly.assertThat(deleteCardResponse.getString("resultStatus")).isEqualTo("SUCCESS");
            softly.assertThat(deleteCardResponse.getString("resultMsg")).isEqualTo("Card deactivated successfully");
            softly.assertAll();
            size--;
        }
        JsonPath afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(jwt, coftMerchant.getId(), custId);
        int savedCardListsize = afterCardDeleteSavedCardResponseJsonPath.getList("response").size();
        String codeDetail=afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail");
        int count=5;
        while (count>0&&codeDetail.equalsIgnoreCase("SUCCESS"))
        {
            afterCardDeleteSavedCardResponseJsonPath = SavedCardHelpersNew.savedCardbyMidCustId(jwt, coftMerchant.getId(), custId);
            codeDetail=afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail");
            savedCardListsize = afterCardDeleteSavedCardResponseJsonPath.getList("response").size();
            count--;
        }
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardListsize).isEqualTo(0);
        softly.assertThat(afterCardDeleteSavedCardResponseJsonPath.getString("codeDetail")).isEqualTo("Card does not exist for given parameters");
        softly.assertAll();
    }
}
