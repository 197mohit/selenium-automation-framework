package scripts.coft.savedCardService;

import com.paytm.api.coft.saveCard.HandlerInternalDeleteBinUser;
import com.paytm.api.coft.saveCard.HanlerInternalBinInfo;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.TokenStatus;
import com.paytm.appconstants.Constants.VAULTIDENTIFIER;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class HandlerInternalDeleteBinUserTests extends PGPBaseTest {

    Constants.MerchantType coftMerchant = Constants.MerchantType.COFT_MERCHANT;

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that user is able to delete active OCL tokens")
    public void VerifyUserIsAbleToDeleteActiveOCLToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);

        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        String savedCardId = binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            savedCardId);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("STATUS")).isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS")).isEqualTo("1");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that proper error message is thrown if incorrect sso is sent in request")
    public void VerifyIncorrectSso() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();

        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding platform card
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);

        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        String savedCardId = binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso + "1",
            savedCardId);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("ErrorCode")).isEqualTo("OAUTH-ERR");
        softly.assertThat(delete.getString("ErrorMsg")).isEqualTo("SSO ID is not found from Oauth");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no card is deleted if incorrect cardId ise sent in request")
    public void VerifyResponseWhenCardNotExist() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);

        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        String savedCardId = binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            savedCardId + 1);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("STATUS")).isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS")).isEqualTo("0");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that user is able to delete active PPBL token")
    public void VerifyUserIsAbleToDeleteActivePPBLToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        System.out.println("Cards are deleted");
        //adding token card in PPBL vault and verifying that is it returned in response
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER, VAULTIDENTIFIER.PPBL.get());
        String ppblToken = SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.PPBL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size ==0) {
            Thread.sleep(5000);
            binInfoResponse = binInfo.execute().jsonPath();
            size = Integer.parseInt(binInfoResponse.getString("SIZE"));
            count++;
        }
        String savedCardId = binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardId).withFailMessage(() -> "Token not added in PPBL vault").isEqualTo(ppblToken);
        softly.assertAll();
        //deleting active PPBL token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            savedCardId);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        softly.assertThat(delete.getString("STATUS")).withFailMessage("Error deleting card").isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS")).withFailMessage("Error deleting card").isEqualTo("1");
        softly.assertAll();
        //verifying that it is not returned in handlerBinInfo response
        binInfoResponse = binInfo.execute().jsonPath();
        size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            binInfoResponse = binInfo.execute().jsonPath();
            size = Integer.parseInt(binInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).withFailMessage(() -> "Not able to delete Active PPBL Card properly").isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that user is able to delete pending ocl token")
    public void VerifyUserIsAbleToDeletePendingOclToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card on OLC vault
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken = SavedCardHelpers.getTin();
        //moving ocl token to pending state
        SavedCardHelpers.deleteCardOnUser(user, oclToken, VAULTIDENTIFIER.OCL.get());
        SavedCardHelpers.updateTokenStatus(oclToken, TokenStatus.INIT);
        //checking if card is returned in pending tokens
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.OCL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();

        binInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.OCL.get());
        binInfoResponse = binInfo.execute().jsonPath();
        String savedCardId = binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].tokenStatus")).withFailMessage(() -> "Unable to move card to pending state").isEqualTo("INIT");
        softly.assertAll();
        //deleting pending token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            savedCardId);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        softly.assertThat(delete.getString("STATUS")).isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS")).isEqualTo("1");
        softly.assertAll();
        //verify card is not returned after deleting
        binInfo = new HanlerInternalBinInfo(sso, true);
        binInfoResponse = binInfo.execute().jsonPath();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            binInfoResponse = binInfo.execute().jsonPath();
            size = Integer.parseInt(binInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).isEqualTo(0)
            .withFailMessage(() -> "Pending OCL token not deleted properly");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that user is able to delete pending PPBL token")
    public void VerifyUserIsAbleToDeletePendingPPBLToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card on PPBL vault
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER, VAULTIDENTIFIER.PPBL.get());
        String ppblToken = SavedCardHelpers.getTin();
        //moving ppbl token to pending state
        SavedCardHelpers.deleteCardOnUser(user, ppblToken, VAULTIDENTIFIER.PPBL.get());
        SavedCardHelpers.updateTokenStatus(ppblToken,TokenStatus.INIT);
        //checking if card is returned in pending tokens
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.PPBL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        String savedCardId = binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].tokenStatus")).withFailMessage(() -> "Unable to m move card to pending state").isEqualTo("INIT");
        softly.assertAll();
        //deleting pending ppbl token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            savedCardId);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        softly.assertThat(delete.getString("STATUS")).isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS")).isEqualTo("1");
        softly.assertAll();
        //verify pending ppbl token is deleted and not returned in handlerBinInfo
        binInfo = new HanlerInternalBinInfo(sso, true);
        binInfoResponse = binInfo.execute().jsonPath();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            binInfoResponse = binInfo.execute().jsonPath();
            size = Integer.parseInt(binInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).isEqualTo(0)
            .withFailMessage(() -> "Pending PPBL token not deleted properly");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that OCL token for same card on user is also deleted while deleting PPBL token")
    public void VerifyOCLTokenForSameCardIsAlsoDeletedWhileDeletingPPBLToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card on PPBL vault
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER, VAULTIDENTIFIER.PPBL.get());
        String ppblToken = SavedCardHelpers.getTin();
        //adding card on OCL vault
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken = SavedCardHelpers.getTin();
        //deleting PPBL token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            ppblToken);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("STATUS")).withFailMessage(() -> "Unable to delete card")
            .isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS"))
            .withFailMessage(() -> "Unable to delete card").isEqualTo("1");
        softly.assertAll();
        //verifying PPBL token is deleted and not returned in handlerBinInfo
        HanlerInternalBinInfo ppblBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.PPBL.get());
        JsonPath ppblBinInfoResponse = ppblBinInfo.execute().jsonPath();
        int size = Integer.parseInt(ppblBinInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            ppblBinInfoResponse = ppblBinInfo.execute().jsonPath();
            size = Integer.parseInt(ppblBinInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).withFailMessage(() -> "PPBL token not deleted properly")
            .isEqualTo(0);
        softly.assertThat(ppblBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "PPBL token is not deleted").doesNotContain((ppblToken));
        softly.assertAll();
        //verifying OCL token is also deleted and not returned in handlerBinInfo
        HanlerInternalBinInfo oclBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.OCL.get());
        JsonPath oclBinInfoResponse = oclBinInfo.execute().jsonPath();
        size = Integer.parseInt(oclBinInfoResponse.getString("SIZE"));
        softly.assertThat(size).withFailMessage(() -> "OCL token is not deleted").isEqualTo(0);
        softly.assertThat(oclBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "OCL token is not deleted").doesNotContain((oclToken));
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that PPBL token for same card on user is also deleted while deleting OCL token")
    public void VerifyPPBLTokenForSameCardIsAlsoDeletedWhileDeletingOCLToken() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card on OCL vault
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken = SavedCardHelpers.getTin();
        //adding card on PPBL vault
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER, VAULTIDENTIFIER.PPBL.get());
        String ppblToken = SavedCardHelpers.getTin();
        //deleting OCL token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            oclToken);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("STATUS")).withFailMessage(() -> "Unable to delete card")
            .isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS"))
            .withFailMessage(() -> "Unable to delete card").isEqualTo("1");
        softly.assertAll();
        //verifying OCL token is deleted and not returned in handlerBinInfo
        HanlerInternalBinInfo oclBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.OCL.get());
        JsonPath oclBinInfoResponse = oclBinInfo.execute().jsonPath();
        int size = Integer.parseInt(oclBinInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            oclBinInfoResponse = oclBinInfo.execute().jsonPath();
            size = Integer.parseInt(oclBinInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).withFailMessage(() -> "OCL token not deleted properly")
            .isEqualTo(0);
        softly.assertThat(oclBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "OCL token is not deleted").doesNotContain((oclToken));
        softly.assertAll();
        //verifying PPBL token is also deleted and not returned in handlerBinInfo
        HanlerInternalBinInfo ppblBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.PPBL.get());
        JsonPath ppblBinInfoResponse = ppblBinInfo.execute().jsonPath();
        size = Integer.parseInt(ppblBinInfoResponse.getString("SIZE"));
        softly.assertThat(size).withFailMessage(() -> "PPBL token is not deleted").isEqualTo(0);
        softly.assertThat(ppblBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "PPBL token is not deleted").doesNotContain((ppblToken));
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that PPBL token for different card on user is not deleted while deleting OCL token")
    public void VerifyPPBLTokenForDifferentCardIsNotDeletedWhileDeletingOclCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card on OCL vault
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken = SavedCardHelpers.getTin();
        //adding card on PPBL vault
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD, VAULTIDENTIFIER.PPBL.get());
        String ppblToken = SavedCardHelpers.getTin();
        //deleting OCL token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            oclToken);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("STATUS")).withFailMessage(() -> "Unable to delete card")
            .isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS"))
            .withFailMessage(() -> "Unable to delete card").isEqualTo("1");
        softly.assertAll();
        //verifying OCL token is deleted and not returned in handlerBinInfo
        HanlerInternalBinInfo oclBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.OCL.get());
        JsonPath oclBinInfoResponse = oclBinInfo.execute().jsonPath();
        int size = Integer.parseInt(oclBinInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            oclBinInfoResponse = oclBinInfo.execute().jsonPath();
            size = Integer.parseInt(oclBinInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).withFailMessage(() -> "OCL token is not deleted properly")
            .isEqualTo(0);
        softly.assertThat(oclBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "OCL token is not deleted").doesNotContain(oclToken);
        softly.assertAll();
        //verifying PPBL token is not deleted and returned in handlerBinInfo
        HanlerInternalBinInfo ppblBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.PPBL.get());
        JsonPath ppblBinInfoResponse = ppblBinInfo.execute().jsonPath();
        size = Integer.parseInt(ppblBinInfoResponse.getString("SIZE"));
        softly.assertThat(size).withFailMessage(() -> "PPBL token is also deleted").isEqualTo(1);
        softly.assertThat(ppblBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "PPBL token is also deleted").contains((ppblToken));
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify that OCL token for different card on user is not deleted while deleting PPBL token")
    public void VerifyOCLTokenForDifferentCardIsNotDeletedWhileDeletingPPBLCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        //deleting existing cards on user
        SavedCardHelpers.deleteSavedCard(user);
        //adding token card on OCL vault
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken = SavedCardHelpers.getTin();
        //adding card on PPBL vault
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR,
            PaymentDTO.VISA_COFT_CARD, VAULTIDENTIFIER.PPBL.get());
        String ppblToken = SavedCardHelpers.getTin();
        //deleting PPBL token
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(sso,
            ppblToken);
        JsonPath delete = deleteBinUser.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(delete.getString("STATUS")).withFailMessage(() -> "Unable to delete card")
            .isEqualTo("SUCCESS");
        softly.assertThat(delete.getString("NUMBER_OF_RECORDS"))
            .withFailMessage(() -> "Unable to delete card").isEqualTo("1");
        softly.assertAll();
        //verifying PPBL token is deleted and not returned in handlerBinInfo
        HanlerInternalBinInfo ppblBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.PPBL.get());
        JsonPath ppblBinInfoResponse = ppblBinInfo.execute().jsonPath();
        int size = Integer.parseInt(ppblBinInfoResponse.getString("SIZE"));
        int count = 0;
        while (count < 5 && size != 0) {
            Thread.sleep(5000);
            ppblBinInfoResponse = ppblBinInfo.execute().jsonPath();
            size = Integer.parseInt(ppblBinInfoResponse.getString("SIZE"));
            count++;
        }
        softly.assertThat(size).withFailMessage(() -> "PPBL token is not deleted properly")
            .isEqualTo(0);
        softly.assertThat(ppblBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "PPBL token is not deleted").doesNotContain(ppblToken);
        softly.assertAll();
        //verifying OCL token is not deleted and returned in handlerBinInfo
        HanlerInternalBinInfo oclBinInfo = new HanlerInternalBinInfo(sso, true,
            VAULTIDENTIFIER.OCL.get());
        JsonPath oclBinInfoResponse = oclBinInfo.execute().jsonPath();
        size = Integer.parseInt(oclBinInfoResponse.getString("SIZE"));
        softly.assertThat(size).withFailMessage(() -> "OCL token is also deleted").isEqualTo(1);
        softly.assertThat(oclBinInfoResponse.getList("BIN_DETAILS").toString())
            .withFailMessage(() -> "OCL token is also deleted").contains((oclToken));
        softly.assertAll();
    }
}
