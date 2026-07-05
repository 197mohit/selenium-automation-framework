package scripts.coft.savedCardService;

import com.paytm.ServerConfigProvider.SERVICE;
import com.paytm.api.coft.saveCard.HanlerInternalBinInfo;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.TokenStatus;
import com.paytm.appconstants.Constants.VAULTIDENTIFIER;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.time.Instant;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;

/**
 * @Author abhishek7.verma
 * @Date 30/12/21 11:52 AM
 * @Version 1.0
 */

public class HandlerInternalBinInfoTests extends PGPBaseTest {


    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify tokens are returned when returnToken is sent true")
    public void VerifyReturnTokenTrue() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDBIN")).isEqualTo("476136");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify tokens are not returned when returnToken=false is sent in request")
    public void VerifyReturnTokenFalse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();

        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, false);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getList("BIN_DETAILS").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify only platform cards are returned when returnToken is not sent")
    public void VerifyReturnTokenNotSent() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getList("BIN_DETAILS").size()).isEqualTo(0);
        softly.assertAll();
    }


    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify proper error is thrown when invalid SSO is sent in request")
    public void VerifyError() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();

        SavedCardHelpers.deleteSavedCard(user);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso + "1", true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("ErrorCode")).isEqualTo("OAUTH-ERR");
        softly.assertThat(binInfoResponse.getString("ErrorMsg")).isEqualTo("SSO ID is not found from Oauth");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify No card is returned if not present on user")
    public void VerifyNoCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        softly.assertThat(size).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify PPBL cards are returned in response when vaultIdentifier=PPBL is sent in request")
    public void VerifyPPBLCardsAreReturnedWhenSentInQueryParam() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER,
            VAULTIDENTIFIER.PPBL.get());
        String ppblToken= SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.PPBL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDBIN")).isEqualTo("476136");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(ppblToken);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no OCL cards are returned in response when vaultIdentifier=PPBL is sent in request")
    public void VerifyNoOCLCardsAreReturnedWhenPPBLSentInQueryParam() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER,
            VAULTIDENTIFIER.PPBL.get());
        String ppblToken= SavedCardHelpers.getTin();
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.PPBL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDBIN")).isEqualTo("476136");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(ppblToken);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).doesNotContain(oclToken);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify OCL cards are returned in response when vaultIdentifier=OCL is sent in request")
    public void VerifyOCLCardsAreReturnedWhenSentInQueryParam() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.OCL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDBIN")).isEqualTo("476136");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(oclToken);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no PPBL cards are returned in response when vaultIdentifier=OCL is sent in request")
    public void VerifyNoPPBLCardsAreReturnedWhenOCLSentInQueryParam() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER,
            VAULTIDENTIFIER.PPBL.get());
        String ppblToken= SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.OCL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDBIN")).isEqualTo("476136");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(oclToken);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).doesNotContain(ppblToken);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify pending OCL token is returned when vaultIdentifier=OCL is sent in request")
    public void VerifyForPendingOCLTokens() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,oclToken, VAULTIDENTIFIER.OCL.get());
        SavedCardHelpers.updateTokenStatus(oclToken, TokenStatus.INIT);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.OCL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].tokenStatus")).isEqualTo("INIT");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(oclToken);
        softly.assertAll();
    }


    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no PPBL pending token is returned when vaultIdentifier=OCL is sent in request")
    public void VerifyNoPendingPPBLTokenIsReturned() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,oclToken, VAULTIDENTIFIER.OCL.get());
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.VISA_COFT_CARD_NUMBER,
            VAULTIDENTIFIER.PPBL.get());
        String ppblToken= SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,ppblToken, VAULTIDENTIFIER.PPBL.get());
        SavedCardHelpers.updateTokenStatus(ppblToken,TokenStatus.INIT);
        SavedCardHelpers.updateTokenStatus(oclToken,TokenStatus.INIT);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.OCL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].tokenStatus")).isEqualTo("INIT");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(oclToken);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).doesNotContain(ppblToken);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify PPBL pending tokens are returned when vaultIdentifier=PPBL is sent in request")
    public void VerifyPendingPPBLTokenAreReturned() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.Tokenization_Year, PaymentDTO.VISA_COFT_CARD_NUMBER,
            VAULTIDENTIFIER.PPBL.get());
        String ppblToken= SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,ppblToken, VAULTIDENTIFIER.PPBL.get());
        SavedCardHelpers.updateTokenStatus(ppblToken,TokenStatus.INIT);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.PPBL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].tokenStatus")).isEqualTo("INIT");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(ppblToken);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no OCL pending token is returned when vaultIdentifier=PPBL is sent in request")
    public void VerifyNoPendingOCLTokenIsReturned() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER,
            VAULTIDENTIFIER.PPBL.get());
        String ppblToken= SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,oclToken, VAULTIDENTIFIER.OCL.get());
        SavedCardHelpers.deleteCardOnUser(user,ppblToken, VAULTIDENTIFIER.PPBL.get());
        SavedCardHelpers.updateTokenStatus(oclToken,TokenStatus.INIT);
        SavedCardHelpers.updateTokenStatus(ppblToken,TokenStatus.INIT);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.PPBL.get());
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].CARDLASTDIGIT")).isEqualTo(PaymentDTO.VISA_COFT_CARD_LAST_FOUR_NUMBER);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS[0].tokenStatus")).isEqualTo("INIT");
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).contains(ppblToken);
        softly.assertThat(binInfoResponse.getString("BIN_DETAILS")).doesNotContain(oclToken);
        softly.assertAll();
    }

    //todo
   // @Owner(Constants.Owner.ABHISHEK_VERMA)
   // @Test(description = "Verify vaultIdentifier is going in fetch-all and fetch-pendingtoken request",enabled = false)
    public void VerifyVaultIdentifierInFetchAllAndFetchPendingTokenRequest() throws Exception {
        String X_APP_RID = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        User user = userManager.getForWrite(Label.BASIC);
        String sso = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
        String oclToken= SavedCardHelpers.getTin();
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true, VAULTIDENTIFIER.OCL.get(),X_APP_RID);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        String expectedVault=VAULTIDENTIFIER.OCL.toString();
        String cmdToFetchSavedCardLogger=" grep \"" + X_APP_RID + "\" /paytm/logs/savedcardservic_facade.log" + " | grep \"ASSET_CENTER_SERVICE\"";
        String savedCardLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(
            SERVICE.SAVEDCARDSERVICE, cmdToFetchSavedCardLogger), s -> !"".equals(s));
        Assertions.assertThat(savedCardLogger.contains(expectedVault));
    }

}
