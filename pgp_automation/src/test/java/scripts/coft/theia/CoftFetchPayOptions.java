package scripts.coft.theia;

import com.paytm.api.coft.saveCard.HandlerInternalDeleteBinUser;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.SavedInstruments;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

public class CoftFetchPayOptions extends PGPBaseTest {
    Constants.MerchantType coftMerchant = Constants.MerchantType.COFT_MERCHANT;
    String cin = "";

    @Test (description = "Verify coft parameters are coming in FPO")
    public void verifyCoftParametersForCin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String  ssoToken= user.ssoToken();
        HandlerInternalDeleteBinUser.deleteAllCards(ssoToken);
        PGPHelpers.saveCardAtAlipayUserBind(user.custId(), "05", "2025", "5506900480000016");
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
                    .setTxnValue("2000")
                    .setSsoToken(ssoToken)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                    .setTxnToken(txnToken)
                    .setTokenType("TXN_TOKEN")
                    .setVersion("v1")
                    .setMid(coftMerchant.getId())
                    .build();
            FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(coftMerchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO, false);
           SavedInstruments savedInstruments= fetchPaymentOptResponse.getBody().getMerchantPayOption().getSavedInstruments().get(0);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(savedInstruments.getIsCardCoft()).isEqualTo("false");
        softAssertions.assertThat(savedInstruments.getIsEligibleForCoft()).isEqualTo("true");
        softAssertions.assertThat(savedInstruments.getIsCoftPaymentSupported()).isEqualTo("false");
        softAssertions.assertAll();

    }

    @Test
    public void verifyCoftParametersForTin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String  ssoToken= user.ssoToken();
        HandlerInternalDeleteBinUser.deleteAllCards(ssoToken);
        JsonPath tokenizedCardResponse = SavedCardHelpersNew.tokenizeCard(coftMerchant, user, "VISA");
        String tin = tokenizedCardResponse.getString("body.tokenInfo.tokenIndexNumber");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
                .setTxnValue("2000")
                .setSsoToken(ssoToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTxnToken(txnToken)
                .setTokenType("TXN_TOKEN")
                .setVersion("v1")
                .setMid(coftMerchant.getId())
                .build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(coftMerchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO, false);
        SavedInstruments savedInstruments= fetchPaymentOptResponse.getBody().getMerchantPayOption().getSavedInstruments().get(0);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(savedInstruments.getCardDetails().getCardId()).isEqualTo(tin);
        softAssertions.assertThat(savedInstruments.getIsCardCoft()).isEqualTo("true");
        softAssertions.assertThat(savedInstruments.getIsEligibleForCoft()).isEqualTo("false");
        softAssertions.assertThat(savedInstruments.getIsCoftPaymentSupported()).isEqualTo("true");
        softAssertions.assertAll();

    }

    @Test
    public void verifyCoftParametersForCinAndTin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String  ssoToken= user.ssoToken();
        HandlerInternalDeleteBinUser.deleteAllCards(ssoToken);
        PGPHelpers.saveCardAtAlipayUserBind(user.custId(), "05", "2025", "4761360075860436");
        JsonPath tokenizedCardResponse = SavedCardHelpersNew.tokenizeCard(coftMerchant, user, "VISA");
        String tin = tokenizedCardResponse.getString("body.tokenInfo.tokenIndexNumber");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
                .setTxnValue("2000")
                .setSsoToken(ssoToken)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTxnToken(txnToken)
                .setTokenType("TXN_TOKEN")
                .setVersion("v1")
                .setMid(coftMerchant.getId())
                .build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(coftMerchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO, false);
        SavedInstruments savedInstruments= fetchPaymentOptResponse.getBody().getMerchantPayOption().getSavedInstruments().get(0);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(savedInstruments.getCardDetails().getCardId()).isEqualTo(tin);
        softAssertions.assertThat(savedInstruments.getIsCardCoft()).isEqualTo("true");
        softAssertions.assertThat(savedInstruments.getIsEligibleForCoft()).isEqualTo("false");
        softAssertions.assertThat(savedInstruments.getIsCoftPaymentSupported()).isEqualTo("true");
        softAssertions.assertAll();

    }

}
